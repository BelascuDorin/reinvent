package com.reinvent.mentorship.internal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Mentor profile read-side and its provisioning (spec 0002). Profiles are created
 * as a draft when the Reviewer approves a Mentor — an intra-module reaction driven by
 * {@link MentorshipService#approve}, with no new cross-module event. Discoverability
 * composes the profile-local completeness rule with the application's approved-and-
 * not-suspended state; both live in this module, so this service reads the application
 * repository directly rather than depending on {@link MentorshipService} (which would
 * form a cycle, since approval calls back here to provision).
 */
@Service
@Transactional
class MentorProfileService {

	private final MentorProfileRepository profiles;
	private final MentorApplicationRepository applications;

	MentorProfileService(MentorProfileRepository profiles, MentorApplicationRepository applications) {
		this.profiles = profiles;
		this.applications = applications;
	}

	/**
	 * Provision a draft profile for a newly approved Mentor. Idempotent: a User keeps
	 * their existing profile (and its filled-in data) rather than having it reset.
	 */
	void provisionDraft(UUID mentorUserId, Instant now) {
		if (!profiles.existsById(mentorUserId)) {
			profiles.save(MentorProfile.draft(mentorUserId, now));
		}
	}

	/** The caller's own profile, or 404 if they were never approved as a Mentor. */
	@Transactional(readOnly = true)
	MentorProfileView myProfile(UUID userId) {
		MentorProfile profile = profiles.findById(userId).orElseThrow(MentorProfileNotFoundException::new);
		boolean complete = profile.isComplete();
		// Discovery visibility: complete AND still an active Mentor. Deliberately weaker
		// than MentorBookability — payment onboarding is a booking-time concern, not a
		// discovery one.
		boolean discoverable = complete && isActiveMentor(userId);
		return MentorProfileView.of(profile, complete, discoverable);
	}

	private boolean isActiveMentor(UUID userId) {
		return applications.findFirstByApplicantUserIdOrderByCreatedAtDesc(userId)
				.map(MentorApplication::isActiveMentor)
				.orElse(false);
	}
}
