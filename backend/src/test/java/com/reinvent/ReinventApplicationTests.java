package com.reinvent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Smoke test: the whole application context starts against a real Postgres
 * (Testcontainers) with Flyway migrations applied. If the skeleton walks, this
 * passes.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReinventApplicationTests {

	@Test
	void contextLoads() {
	}
}
