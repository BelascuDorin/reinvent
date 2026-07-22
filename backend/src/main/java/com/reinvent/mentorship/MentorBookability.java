package com.reinvent.mentorship;

import java.util.UUID;

/**
 * Whether a Mentor may currently be booked. Defined here as the extension point the
 * booking slice will consume; nothing calls it yet in this slice.
 *
 * <p>The predicate (ADR-0001 + ADR-0003): a Mentor is bookable only when their
 * application is APPROVED, they are not suspended, and the payments provider reports
 * payout onboarding complete.
 */
public interface MentorBookability {

	boolean isBookable(UUID mentorUserId);
}
