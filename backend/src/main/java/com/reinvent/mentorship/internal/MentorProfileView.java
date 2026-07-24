package com.reinvent.mentorship.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * The owner's view of their own Mentor profile: the presentation fields plus the two
 * derived flags a Mentor needs to know where they stand — {@code complete} (the
 * profile-local rule: at least one Field, a price, and a Meeting duration) and
 * {@code discoverable} (that, and the application approved and not suspended). A
 * freshly provisioned draft reports both false.
 */
record MentorProfileView(
		UUID mentorUserId,
		String displayName,
		String roleTitle,
		String bio,
		String experience,
		String employer,
		BigDecimal priceAmount,
		String priceCurrency,
		Integer meetingDurationMinutes,
		List<String> fieldSlugs,
		List<String> languages,
		boolean complete,
		boolean discoverable) {

	static MentorProfileView of(MentorProfile profile, boolean complete, boolean discoverable) {
		return new MentorProfileView(
				profile.mentorUserId(),
				profile.displayName(),
				profile.roleTitle(),
				profile.bio(),
				profile.experience(),
				profile.employer(),
				profile.priceAmount(),
				profile.priceCurrency(),
				profile.meetingDurationMinutes(),
				profile.fieldSlugs().stream().sorted().toList(),
				profile.languages().stream().sorted().toList(),
				complete,
				discoverable);
	}
}
