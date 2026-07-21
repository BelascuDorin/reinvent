package com.reinvent.testsupport;

import java.time.Instant;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.reinvent.platform.Clock;
import com.reinvent.platform.testing.FakeClock;

/**
 * Pins the {@link Clock} port to a fixed instant so age/minor determination is
 * deterministic in tests. Import it and treat {@link #FIXED} as "now".
 */
@TestConfiguration(proxyBeanMethods = false)
public class FixedClockConfiguration {

	public static final Instant FIXED = Instant.parse("2026-01-01T00:00:00Z");

	@Bean
	@Primary
	Clock fixedClock() {
		return new FakeClock(FIXED);
	}
}
