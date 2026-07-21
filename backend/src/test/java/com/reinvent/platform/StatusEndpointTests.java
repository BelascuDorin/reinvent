package com.reinvent.platform;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.platform.testing.FakeClock;

/**
 * Drives the status endpoint through the HTTP boundary. Swapping in a
 * {@link FakeClock} pins the time the endpoint reports, proving it reads the
 * {@link Clock} port rather than the wall clock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class StatusEndpointTests {

	static final Instant FIXED = Instant.parse("2026-07-21T00:00:00Z");

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClockConfiguration {

		@Bean
		@Primary
		Clock fixedClock() {
			return new FakeClock(FIXED);
		}
	}

	@Autowired
	MockMvc mvc;

	@Test
	void reportsOkStatusWithTimeFromTheClockPort() throws Exception {
		mvc.perform(get("/api/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("reinvent"))
				.andExpect(jsonPath("$.status").value("ok"))
				.andExpect(jsonPath("$.time").value(FIXED.toString()));
	}
}
