package com.reinvent.identity.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.identity.GuardianConsentStatus;
import com.reinvent.identity.Role;
import com.reinvent.platform.Clock;
import com.reinvent.platform.testing.FakeClock;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Exercises the {@code identity} module in isolation through its service seam
 * (Spring Modulith bootstraps only this module), against a real Postgres. The
 * {@link Clock} port — owned by the platform kernel and therefore not part of an
 * isolated identity context — is supplied here as a fixed test double. It is
 * declared locally (rather than importing the shared config) because
 * {@code @ApplicationModuleTest} only wires beans it finds in the module context.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class IdentityModuleTest {

	@TestConfiguration(proxyBeanMethods = false)
	static class LocalFixedClock {

		@Bean
		Clock clock() {
			return new FakeClock(FixedClockConfiguration.FIXED);
		}
	}

	@Autowired
	IdentityService identity;

	@Test
	void signsUpAMenteeAndAuthenticatesWithTheRightPassword() {
		UUID id = identity.signUp("mod-adult@example.com", "password1", LocalDate.of(1990, 1, 1), null, null);

		assertThat(identity.authenticate("mod-adult@example.com", "password1")).contains(id);
		assertThat(identity.authenticate("mod-adult@example.com", "wrong-password")).isEmpty();

		AccountState state = identity.accountState(id);
		assertThat(state.roles()).containsExactly(Role.MENTEE);
		assertThat(state.minor()).isFalse();
		assertThat(state.guardianConsentStatus()).isEqualTo(GuardianConsentStatus.NOT_REQUIRED);
	}

	@Test
	void aMinorNamesAGuardianAndIsPendingConsent() {
		UUID id = identity.signUp("mod-minor@example.com", "password1", LocalDate.of(2015, 1, 1),
				"Pat Guardian", "guardian@example.com");

		AccountState state = identity.accountState(id);
		assertThat(state.minor()).isTrue();
		assertThat(state.guardianConsentStatus()).isEqualTo(GuardianConsentStatus.PENDING_GUARDIAN_CONSENT);
	}

	@Test
	void aMinorMustNameAGuardian() {
		assertThatThrownBy(() -> identity.signUp("mod-no-guardian@example.com", "password1",
				LocalDate.of(2015, 1, 1), null, null))
				.isInstanceOf(InvalidGuardianDetailsException.class);
	}

	@Test
	void anAdultMustNotNameAGuardian() {
		assertThatThrownBy(() -> identity.signUp("mod-adult-guardian@example.com", "password1",
				LocalDate.of(1990, 1, 1), "Pat Guardian", "guardian@example.com"))
				.isInstanceOf(InvalidGuardianDetailsException.class);
	}
}
