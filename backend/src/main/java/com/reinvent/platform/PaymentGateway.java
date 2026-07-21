package com.reinvent.platform;

import java.util.UUID;

/**
 * Port to the payments provider (Stripe Connect at launch, per ADR-0003).
 *
 * <p>In this walking skeleton it only answers whether a Mentor has completed
 * payout onboarding — the second gate (alongside Reviewer approval) that later
 * makes a Mentor bookable. Charging, escrow, and payouts arrive with the
 * booking slice.
 */
public interface PaymentGateway {

	/**
	 * @return whether the Mentor has completed payout onboarding and can therefore
	 *         receive Payouts.
	 */
	boolean isPayoutOnboardingComplete(UUID mentorId);
}
