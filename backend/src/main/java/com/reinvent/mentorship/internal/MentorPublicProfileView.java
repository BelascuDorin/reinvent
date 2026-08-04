package com.reinvent.mentorship.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A Mentor's whole public presentation (spec 0002), as a visitor with no account reads it
 * and as the Mentor themselves previews it. Deliberately carries no completeness or
 * discoverability bookkeeping: those belong to the owner's edit view, and a visitor
 * seeing this page at all already means the Mentor is discoverable.
 *
 * <p>{@code employer} is dropped from the response entirely when the Mentor left it
 * blank — spec 0002 lets a Mentor mentor without disclosing where they work, so an
 * absent employer should look absent rather than empty. The rating summary is the
 * opposite: always present, and empty until the Reviews slice fills it.
 */
record MentorPublicProfileView(
		UUID mentorUserId,
		String displayName,
		String roleTitle,
		String bio,
		String experience,
		@JsonInclude(JsonInclude.Include.NON_NULL) String employer,
		BigDecimal priceAmount,
		String priceCurrency,
		Integer meetingDurationMinutes,
		List<String> fieldSlugs,
		List<String> languages,
		MentorRatingSummary ratingSummary) {

	static MentorPublicProfileView of(MentorProfile profile) {
		return new MentorPublicProfileView(
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
				null);
	}
}
