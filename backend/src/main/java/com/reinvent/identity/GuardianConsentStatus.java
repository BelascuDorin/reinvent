package com.reinvent.identity;

/**
 * Where a Mentee stands on Guardian consent, as surfaced in their account state.
 *
 * <p>An adult Mentee needs no Guardian ({@link #NOT_REQUIRED}). A minor Mentee
 * starts {@link #PENDING_GUARDIAN_CONSENT} and moves to {@link #CONSENTED} once
 * their Guardian consents (ADR-0002).
 *
 * <p>In this slice the status is derived purely from age; the Guardian-consent
 * flow that actually flips a minor to {@code CONSENTED} lives in the consent
 * module, built in a later slice, which will become the source of truth.
 */
public enum GuardianConsentStatus {
	NOT_REQUIRED,
	PENDING_GUARDIAN_CONSENT,
	CONSENTED
}
