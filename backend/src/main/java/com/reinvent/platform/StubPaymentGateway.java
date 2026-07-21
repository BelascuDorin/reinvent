package com.reinvent.platform;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Placeholder {@link PaymentGateway} for the walking skeleton: no Mentor has
 * onboarded yet, so onboarding is always incomplete. Replaced by the real
 * Stripe Connect adapter in the payments slice.
 */
@Component
class StubPaymentGateway implements PaymentGateway {

	@Override
	public boolean isPayoutOnboardingComplete(UUID mentorId) {
		return false;
	}
}
