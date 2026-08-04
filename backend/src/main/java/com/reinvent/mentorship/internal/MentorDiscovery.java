package com.reinvent.mentorship.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public Mentor discovery (spec 0002): the read-side a visitor with no account browses
 * and searches. Its one rule is who is worth showing — a Mentor is <em>discoverable</em>
 * iff their application is approved and not suspended and their profile is complete.
 * Payment onboarding is deliberately not consulted: that is {@link MentorBookability}'s
 * concern at booking time, which makes discovery the weaker predicate of the two.
 *
 * <p>Both halves of the rule are asked of the objects that own them —
 * {@link MentorProfile#isComplete()} and {@link MentorApplication#isActiveMentor()} —
 * rather than restated as a query, so there is exactly one definition of each. The
 * search criteria (Field, language) are all that the database filters on.
 */
@Service
@Transactional(readOnly = true)
class MentorDiscovery {

	/**
	 * A stable browse order, so the same search always reads the same way: by display
	 * name, with the Mentor id breaking ties (two Mentors may share a name, and a
	 * complete profile is not obliged to have a display name at all).
	 */
	private static final Comparator<MentorProfile> BROWSE_ORDER = Comparator
			.comparing(MentorProfile::displayName, Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing(MentorProfile::mentorUserId);

	private final MentorProfileRepository profiles;
	private final MentorDiscoverability discoverability;

	MentorDiscovery(MentorProfileRepository profiles, MentorDiscoverability discoverability) {
		this.profiles = profiles;
		this.discoverability = discoverability;
	}

	/**
	 * The discoverable Mentors in a Field (the primary axis), optionally narrowed to
	 * those speaking a language. Either criterion may be blank or absent, which means
	 * "don't narrow on it". A search nobody matches is an empty list, not an error.
	 */
	List<MentorSummaryView> search(String fieldSlug, String language) {
		List<MentorProfile> matching = profiles.findMatching(omittedIfBlank(fieldSlug), omittedIfBlank(language));
		Set<UUID> activeMentors = discoverability
				.activeMentorsAmong(matching.stream().map(MentorProfile::mentorUserId).toList());

		return matching.stream()
				.filter(profile -> activeMentors.contains(profile.mentorUserId()))
				.filter(MentorProfile::isComplete)
				.sorted(BROWSE_ORDER)
				.map(MentorSummaryView::of)
				.toList();
	}

	/**
	 * One Mentor's whole public presentation, by the stable identifier a summary carries.
	 * Only a discoverable Mentor has one: an incomplete or suspended Mentor is not found
	 * at all rather than shown half-finished, which is the same rule the browse list
	 * applies — completeness gates visibility, and it gates it everywhere.
	 */
	MentorPublicProfileView publicProfile(UUID mentorUserId) {
		return profiles.findById(mentorUserId)
				.filter(discoverability::isDiscoverable)
				.map(MentorPublicProfileView::of)
				.orElseThrow(MentorProfileNotFoundException::notPubliclyAvailable);
	}

	/** An empty search box narrows nothing, so blank reads the same as absent. */
	private static String omittedIfBlank(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}
}
