package com.reinvent.mentorship.internal;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reinvent.mentorship.MentorBookability;
import com.reinvent.platform.Clock;
import com.reinvent.platform.MentorApproved;
import com.reinvent.platform.PaymentGateway;

/**
 * The Mentor application lifecycle and Reviewer actions (ADR-0001). The aggregate
 * enforces legal transitions; this service coordinates persistence, the bookability
 * predicate, and — on approval — announces {@link MentorApproved} so identity grants
 * the MENTOR role. Authorization is the controllers' concern (via CurrentUser); the
 * service trusts the ids it is given.
 */
@Service
@Transactional
class MentorshipService implements MentorBookability {

	/** Statuses that stop a User opening another application (derived from the rule
	 * the status enum owns), for the duplicate-check query. */
	private static final List<ApplicationStatus> BLOCKING = Arrays.stream(ApplicationStatus.values())
			.filter(ApplicationStatus::blocksReapplication).toList();

	/** The Reviewer queue: applications still awaiting a decision. */
	private static final List<ApplicationStatus> PENDING = List.of(
			ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW);

	private final MentorApplicationRepository applications;
	private final MentorProfileService mentorProfiles;
	private final PaymentGateway paymentGateway;
	private final Clock clock;
	private final ApplicationEventPublisher events;

	MentorshipService(MentorApplicationRepository applications, MentorProfileService mentorProfiles,
			PaymentGateway paymentGateway, Clock clock, ApplicationEventPublisher events) {
		this.applications = applications;
		this.mentorProfiles = mentorProfiles;
		this.paymentGateway = paymentGateway;
		this.clock = clock;
		this.events = events;
	}

	/** A User applies for the Mentor role; at most one open or approved application. */
	MentorApplicationView apply(UUID applicantUserId, MentorApplicationRequest request) {
		if (applications.existsByApplicantUserIdAndStatusIn(applicantUserId, BLOCKING)) {
			throw new DuplicateApplicationException();
		}
		int attempt = applications.countByApplicantUserId(applicantUserId) + 1;
		MentorApplication application = new MentorApplication(UUID.randomUUID(), applicantUserId, attempt,
				request.headline().strip(), request.bio().strip(), clock.now());
		return view(applications.save(application));
	}

	/** The applicant's current application, or 404 if they have never applied. */
	@Transactional(readOnly = true)
	MentorApplicationView myApplication(UUID applicantUserId) {
		return MentorApplication.current(applications.findByApplicantUserId(applicantUserId))
				.map(this::view)
				.orElseThrow(ApplicationNotFoundException::new);
	}

	@Transactional(readOnly = true)
	List<MentorApplicationView> queue() {
		return applications.findByStatusInOrderByCreatedAtAsc(PENDING).stream().map(this::view).toList();
	}

	/** Approved Mentors, so a Reviewer can find one to suspend. */
	@Transactional(readOnly = true)
	List<MentorApplicationView> approvedMentors() {
		return applications.findByStatusInOrderByCreatedAtAsc(List.of(ApplicationStatus.APPROVED))
				.stream().map(this::view).toList();
	}

	MentorApplicationView startReview(UUID applicationId) {
		MentorApplication application = require(applicationId);
		application.startReview(clock.now());
		return view(application);
	}

	MentorApplicationView approve(UUID applicationId) {
		MentorApplication application = require(applicationId);
		application.approve(clock.now());
		// Give the new Mentor something to start from: a draft profile seeded with what
		// they wrote when applying, provisioned intra-module (no new platform event). The
		// MentorApproved event below still tells identity to grant the MENTOR role.
		mentorProfiles.provisionDraft(application.applicantUserId(), application.headline(), application.bio(),
				clock.now());
		events.publishEvent(new MentorApproved(application.applicantUserId()));
		return view(application);
	}

	MentorApplicationView reject(UUID applicationId, String reason) {
		MentorApplication application = require(applicationId);
		application.reject(reason, clock.now());
		return view(application);
	}

	MentorApplicationView suspend(UUID applicationId) {
		MentorApplication application = require(applicationId);
		application.suspend(clock.now());
		return view(application);
	}

	MentorApplicationView reinstate(UUID applicationId) {
		MentorApplication application = require(applicationId);
		application.reinstate(clock.now());
		return view(application);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isBookable(UUID mentorUserId) {
		return MentorApplication.current(applications.findByApplicantUserId(mentorUserId))
				.map(this::isBookable)
				.orElse(false);
	}

	private MentorApplication require(UUID applicationId) {
		return applications.findById(applicationId).orElseThrow(ApplicationNotFoundException::new);
	}

	private MentorApplicationView view(MentorApplication application) {
		return MentorApplicationView.of(application, isBookable(application));
	}

	private boolean isBookable(MentorApplication application) {
		return application.isActiveMentor()
				&& paymentGateway.isPayoutOnboardingComplete(application.applicantUserId());
	}
}
