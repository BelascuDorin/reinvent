package com.reinvent.mentorship.internal;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Mentor's edit of their own profile (spec 0002 ticket #9): a full replacement of
 * the presentation fields, the chosen Field(s), and the languages. There is no publish
 * step — filling in the required parts (≥1 Field, a price, a Meeting duration) is what
 * makes the Mentor discoverable, so this single update both edits and (un)publishes.
 *
 * <p>{@code employer} is optional and may be blank; every other text field is stored
 * as sent. {@code fieldSlugs} must all be curated Fields or the update is rejected.
 * Null collections are treated as empty.
 */
record UpdateMentorProfileRequest(
		String displayName,
		String roleTitle,
		String bio,
		String experience,
		String employer,
		BigDecimal priceAmount,
		String priceCurrency,
		Integer meetingDurationMinutes,
		List<String> fieldSlugs,
		List<String> languages) {
}
