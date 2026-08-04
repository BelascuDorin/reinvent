package com.reinvent.mentorship.internal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reinvent.platform.Clock;

/**
 * The Mentor profile: its provisioning, the owner's edits, and the owner's read
 * (spec 0002). Profiles are created
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
	private final FieldCatalog fieldCatalog;
	private final Clock clock;

	MentorProfileService(MentorProfileRepository profiles, MentorApplicationRepository applications,
			FieldCatalog fieldCatalog, Clock clock) {
		this.profiles = profiles;
		this.applications = applications;
		this.fieldCatalog = fieldCatalog;
		this.clock = clock;
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
		return view(profile);
	}

	/**
	 * Apply the owner's edit to their own profile. The caller's id is the profile key,
	 * so a Mentor can only ever edit their own (404 if they were never approved).
	 * Chosen Fields are validated against the curated set — an out-of-set slug is
	 * rejected — and the returned view reflects the new completeness/discoverability,
	 * which flips live with no separate publish step.
	 */
	MentorProfileView updateMyProfile(UUID userId, UpdateMentorProfileRequest edit) {
		MentorProfile profile = profiles.findById(userId).orElseThrow(MentorProfileNotFoundException::new);
		if (!fieldCatalog.containsAll(edit.fieldSlugs())) {
			throw new UnknownFieldException();
		}
		profile.update(edit, clock.now());
		return view(profile);
	}

	/**
	 * Assemble the owner's view, composing the profile-local completeness rule with the
	 * Mentor's active state. Discovery visibility is complete AND still an active Mentor
	 * — deliberately weaker than MentorBookability, which additionally gates on payment
	 * onboarding (a booking-time concern, not a discovery one).
	 */
	private MentorProfileView view(MentorProfile profile) {
		boolean complete = profile.isComplete();
		boolean discoverable = complete && isActiveMentor(profile.mentorUserId());
		return MentorProfileView.of(profile, complete, discoverable);
	}

	private boolean isActiveMentor(UUID userId) {
		return applications.findFirstByApplicantUserIdOrderByCreatedAtDesc(userId)
				.map(MentorApplication::isActiveMentor)
				.orElse(false);
	}
}
