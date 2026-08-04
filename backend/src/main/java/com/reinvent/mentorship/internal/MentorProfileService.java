package com.reinvent.mentorship.internal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reinvent.platform.Clock;

/**
 * The Mentor profile as its owner deals with it (spec 0002): provisioning, their edits,
 * their own read, and the preview of how the public sees them. Profiles are created as a
 * draft when the Reviewer approves a Mentor — an intra-module reaction driven by
 * {@link MentorshipService#approve}, with no new cross-module event. Whether the Mentor
 * is discoverable is not decided here: {@link MentorDiscoverability} owns that rule, so
 * the owner's indicator and the public listings can never disagree.
 */
@Service
@Transactional
class MentorProfileService {

	private final MentorProfileRepository profiles;
	private final MentorDiscoverability discoverability;
	private final FieldCatalog fieldCatalog;
	private final Clock clock;

	MentorProfileService(MentorProfileRepository profiles, MentorDiscoverability discoverability,
			FieldCatalog fieldCatalog, Clock clock) {
		this.profiles = profiles;
		this.discoverability = discoverability;
		this.fieldCatalog = fieldCatalog;
		this.clock = clock;
	}

	/**
	 * Provision a draft profile for a newly approved Mentor, seeded from what they wrote
	 * when applying. Guarded rather than blind: a User who somehow already has a profile
	 * keeps it, so seeding can never overwrite words a Mentor wrote for themselves. The
	 * lifecycle should not allow a second approval per User anyway — the guard is there so
	 * that assumption failing is harmless rather than destructive.
	 */
	void provisionDraft(UUID mentorUserId, String headline, String bio, Instant now) {
		if (!profiles.existsById(mentorUserId)) {
			profiles.save(MentorProfile.draft(mentorUserId, headline, bio, now));
		}
	}

	/** The caller's own profile, or 404 if they were never approved as a Mentor. */
	@Transactional(readOnly = true)
	MentorProfileView myProfile(UUID userId) {
		MentorProfile profile = profiles.findById(userId).orElseThrow(MentorProfileNotFoundException::mine);
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
		MentorProfile profile = profiles.findById(userId).orElseThrow(MentorProfileNotFoundException::mine);
		if (!fieldCatalog.containsAll(edit.fieldSlugs())) {
			throw new UnknownFieldException();
		}
		profile.update(edit, clock.now());
		return view(profile);
	}

	/**
	 * The caller's own profile as a Mentee would see it — the preview that answers "what
	 * will they actually read?". Deliberately the same view the public endpoint returns,
	 * so the two can't drift, and deliberately available while the profile is still
	 * incomplete: checking the presentation is part of finishing it.
	 */
	@Transactional(readOnly = true)
	MentorPublicProfileView previewMyProfile(UUID userId) {
		MentorProfile profile = profiles.findById(userId).orElseThrow(MentorProfileNotFoundException::mine);
		return MentorPublicProfileView.of(profile);
	}

	/** Assemble the owner's view, telling them where they stand. */
	private MentorProfileView view(MentorProfile profile) {
		return MentorProfileView.of(profile, profile.isComplete(), discoverability.isDiscoverable(profile));
	}
}
