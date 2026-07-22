package com.reinvent.consent.internal;

/**
 * How a consent link stands when the Guardian opens it, for display. Unlike the
 * persistent {@link ConsentStatus}, this includes {@link #EXPIRED}, which is
 * derived from the clock rather than stored.
 */
enum ConsentLinkState {
	/** Still open for the Guardian to consent. */
	PENDING,
	/** Already consented — the link is spent. */
	CONSENTED,
	/** The validity window has lapsed; a fresh link is needed. */
	EXPIRED
}
