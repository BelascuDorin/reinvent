package com.reinvent.mentorship.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one definition of who Reinvent will show to the public (spec 0002): a Mentor is
 * <em>discoverable</em> iff their application is approved and not suspended and their
 * profile is complete. Everything that needs the rule — the browse list, a single public
 * profile, and the Mentor's own "am I discoverable yet" indicator — asks here, so the
 * rule cannot drift between them.
 *
 * <p>Each half is still owned by the object it belongs to
 * ({@link MentorProfile#isComplete()} and {@link MentorApplication#isActiveMentor()});
 * this composes them and nothing more.
 *
 * <p>Deliberately weaker than {@link MentorBookability}, which additionally requires
 * payment onboarding: being findable is not the same as being bookable, and payment is a
 * booking-time concern.
 */
@Service
@Transactional(readOnly = true)
class MentorDiscoverability {

	private final MentorApplicationRepository applications;

	MentorDiscoverability(MentorApplicationRepository applications) {
		this.applications = applications;
	}

	/** Whether this profile's Mentor is currently shown to the public. */
	boolean isDiscoverable(MentorProfile profile) {
		return profile.isComplete() && isActiveMentor(profile.mentorUserId());
	}

	/**
	 * Whether this User is an active Mentor right now, read from their latest application
	 * (a rejected User may have reapplied, so older rows are history).
	 */
	private boolean isActiveMentor(UUID userId) {
		return applications.findFirstByApplicantUserIdOrderByCreatedAtDesc(userId)
				.map(MentorApplication::isActiveMentor)
				.orElse(false);
	}

	/**
	 * Which of these Users are active Mentors — the same rule as
	 * {@link #isActiveMentor(UUID)}, asked about a batch in one query so listing many
	 * Mentors doesn't cost a query each.
	 */
	Set<UUID> activeMentorsAmong(Collection<UUID> userIds) {
		if (userIds.isEmpty()) {
			return Set.of();
		}
		Map<UUID, MentorApplication> latest = new HashMap<>();
		for (MentorApplication application : applications.findByApplicantUserIdInOrderByCreatedAtDesc(userIds)) {
			latest.putIfAbsent(application.applicantUserId(), application);
		}
		return latest.values().stream()
				.filter(MentorApplication::isActiveMentor)
				.map(MentorApplication::applicantUserId)
				.collect(Collectors.toSet());
	}
}
