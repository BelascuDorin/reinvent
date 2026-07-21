package com.reinvent.platform;

import java.time.Instant;

import org.springframework.stereotype.Component;

/**
 * Production {@link Clock}: the one place in the system that is allowed to read
 * the wall clock.
 */
@Component
class SystemClock implements Clock {

	@Override
	public Instant now() {
		return Instant.now();
	}
}
