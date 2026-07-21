package com.reinvent.platform.testing;

import java.time.Instant;

import com.reinvent.platform.Clock;

/**
 * In-memory {@link Clock} fake: time only moves when a test moves it, so
 * expiry and window behaviour is deterministic.
 */
public class FakeClock implements Clock {

	private Instant now;

	public FakeClock(Instant now) {
		this.now = now;
	}

	public void set(Instant now) {
		this.now = now;
	}

	@Override
	public Instant now() {
		return now;
	}
}
