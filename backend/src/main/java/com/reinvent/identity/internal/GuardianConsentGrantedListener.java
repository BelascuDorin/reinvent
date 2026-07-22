package com.reinvent.identity.internal;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.reinvent.platform.GuardianConsentGranted;

/**
 * Lifts a minor Mentee's {@code PENDING_GUARDIAN_CONSENT} gate when the consent
 * module reports the Guardian consented. The consent module owns the consent
 * record; identity keeps only this account-state projection, updated on the event.
 *
 * <p>{@link ApplicationModuleListener} runs the handler transactionally and after
 * the publishing transaction commits, so the two modules stay decoupled.
 */
@Component
class GuardianConsentGrantedListener {

	private final UserRepository users;

	GuardianConsentGrantedListener(UserRepository users) {
		this.users = users;
	}

	@ApplicationModuleListener
	void on(GuardianConsentGranted event) {
		users.findById(event.userId()).ifPresent(User::markGuardianConsented);
	}
}
