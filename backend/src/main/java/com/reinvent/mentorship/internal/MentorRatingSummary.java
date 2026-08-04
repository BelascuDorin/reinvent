package com.reinvent.mentorship.internal;

import java.math.BigDecimal;

/**
 * The rating summary shown beside a Mentor in discovery: the slot spec 0002 reserves
 * ("holds a slot for a rating summary that stays empty until the Reviews slice
 * populates it"). Nothing produces one yet — reviews arrive in a later spec — so it
 * serialises as null on every summary today. It exists now so the response shape a
 * Mentee's client reads doesn't have to change when reviews land.
 */
record MentorRatingSummary(BigDecimal averageRating, int reviewCount) {
}
