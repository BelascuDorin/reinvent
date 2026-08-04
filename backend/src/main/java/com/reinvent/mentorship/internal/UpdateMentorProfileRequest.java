package com.reinvent.mentorship.internal;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * The Mentor's edit of their own profile (spec 0002 ticket #9): a full replacement of
 * the presentation fields, the chosen Field(s), and the languages. There is no publish
 * step — filling in the required parts (≥1 Field, a price, a Meeting duration) is what
 * makes the Mentor discoverable, so this single update both edits and (un)publishes.
 *
 * <p>Every text field is optional and may be sent blank — a Mentor is allowed to save a
 * half-finished profile, which simply reads back as not complete; blank is normalised to
 * absent. {@code fieldSlugs} must all be curated Fields or the update is rejected. Null
 * collections are treated as empty. A price or Meeting duration, when given, must be a
 * sensible quantity, so an absurd figure can't masquerade as a complete profile.
 */
record UpdateMentorProfileRequest(
		@Size(max = 200) String displayName,
		@Size(max = 200) String roleTitle,
		@Size(max = 5000) String bio,
		@Size(max = 5000) String experience,
		@Size(max = 200) String employer,
		@PositiveOrZero BigDecimal priceAmount,
		@Size(max = 3) String priceCurrency,
		@Positive Integer meetingDurationMinutes,
		List<String> fieldSlugs,
		List<String> languages) {
}
