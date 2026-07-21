package com.reinvent.platform;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;

import com.reinvent.TestcontainersConfiguration;

/**
 * Bootstraps the {@code platform} module in isolation via Spring Modulith. This
 * is the reference per-module test seam every later feature slice inherits:
 * {@code @ApplicationModuleTest} boots one module against a real Postgres, with
 * external systems available as ports.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class PlatformModuleTest {

	@Autowired
	PaymentGateway paymentGateway;

	@Autowired
	VideoProvider videoProvider;

	@Autowired
	GuardianNotifier guardianNotifier;

	@Autowired
	Clock clock;

	@Test
	void bootstrapsWithItsPortsWired() {
		assertThat(paymentGateway).isNotNull();
		assertThat(videoProvider).isNotNull();
		assertThat(guardianNotifier).isNotNull();
		assertThat(clock).isNotNull();
	}
}
