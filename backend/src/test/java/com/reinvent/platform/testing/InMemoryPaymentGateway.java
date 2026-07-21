package com.reinvent.platform.testing;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.reinvent.platform.PaymentGateway;

/**
 * In-memory {@link PaymentGateway} fake: a test marks which Mentors have
 * completed payout onboarding.
 */
public class InMemoryPaymentGateway implements PaymentGateway {

	private final Set<UUID> onboarded = new HashSet<>();

	public void markOnboarded(UUID mentorId) {
		onboarded.add(mentorId);
	}

	@Override
	public boolean isPayoutOnboardingComplete(UUID mentorId) {
		return onboarded.contains(mentorId);
	}
}
