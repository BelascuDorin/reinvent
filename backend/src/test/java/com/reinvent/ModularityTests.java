package com.reinvent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Enforces the module structure. This is a fast, context-free check that fails
 * the build if a module reaches into another module's internals or if the
 * declared modules drift.
 */
class ModularityTests {

	static final ApplicationModules modules = ApplicationModules.of(ReinventApplication.class);

	@Test
	void verifiesModuleStructure() {
		modules.verify();
	}

	@Test
	void declaresTheExpectedModules() {
		assertThat(modules.stream().map(module -> module.getIdentifier().toString()))
				.contains("identity", "mentorship", "consent", "platform");
	}
}
