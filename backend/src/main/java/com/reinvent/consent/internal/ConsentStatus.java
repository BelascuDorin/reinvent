package com.reinvent.consent.internal;

/**
 * Persistent state of a Guardian consent request. Expiry is not a stored state —
 * it is derived by comparing the {@link com.reinvent.platform.Clock} to the
 * request's expiry instant, so a link that lapses needs no write to "become"
 * expired.
 */
enum ConsentStatus {
	/** Awaiting the Guardian's action; still consumable if not yet expired. */
	PENDING,
	/** The Guardian has consented; the link is spent and cannot be reused. */
	CONSENTED
}
