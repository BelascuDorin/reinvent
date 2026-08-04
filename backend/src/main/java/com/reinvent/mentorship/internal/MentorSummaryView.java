package com.reinvent.mentorship.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * How a Mentor appears in public discovery (spec 0002): just enough for a Mentee to
 * decide who is worth opening — the fields the discovery contract lists and no more, so
 * bio, experience, and employer stay with the full public profile. Only discoverable
 * Mentors are ever rendered as one, so there is no completeness flag here: a summary
 * existing already means the Mentor is discoverable. The Mentor's User id is the stable
 * identifier a client uses to open their profile.
 */
record MentorSummaryView(
		UUID mentorUserId,
		String displayName,
		String roleTitle,
		BigDecimal priceAmount,
		String priceCurrency,
		Integer meetingDurationMinutes,
		List<String> fieldSlugs,
		List<String> languages,
		MentorRatingSummary ratingSummary) {

	static MentorSummaryView of(MentorProfile profile) {
		return new MentorSummaryView(
				profile.mentorUserId(),
				profile.displayName(),
				profile.roleTitle(),
				profile.priceAmount(),
				profile.priceCurrency(),
				profile.meetingDurationMinutes(),
				profile.fieldSlugs().stream().sorted().toList(),
				profile.languages().stream().sorted().toList(),
				null);
	}
}
