package com.reinvent.platform;

import java.time.Instant;

/**
 * The single source of "now" for the whole system.
 *
 * <p>Domain code must depend on this port rather than reading the wall clock
 * directly, so time-sensitive behaviour (consent-link expiry, and later the
 * dispute-hold and payout windows) is deterministic under test.
 */
public interface Clock {

	Instant now();
}
