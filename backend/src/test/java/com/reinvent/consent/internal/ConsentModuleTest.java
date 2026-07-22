package com.reinvent.consent.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.platform.Clock;
import com.reinvent.platform.GuardianConsentGranted;
import com.reinvent.platform.GuardianNotifier;
import com.reinvent.platform.MinorMenteeRegistered;
import com.reinvent.platform.testing.FakeClock;
import com.reinvent.platform.testing.InMemoryGuardianNotifier;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Exercises the {@code consent} module in isolation (Spring Modulith bootstraps only
 * this module) against a real Postgres, with the two ports it touches supplied as
 * fakes: {@link GuardianNotifier} captures the link, {@link Clock} makes expiry
 * deterministic. Reads as the Guardian's story: a minor registers → a single-use
 * link is sent → the Guardian consents once → identity is told, and a spent or
 * expired link is refused.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class ConsentModuleTest {

	@TestConfiguration(proxyBeanMethods = false)
	static class Fakes {

		// Each fake is its concrete type so tests can autowire it to drive/inspect,
		// and — being the only bean of its port type in an isolated module context —
		// satisfies the port the service depends on without a @Primary.
		@Bean
		FakeClock clock() {
			return new FakeClock(FixedClockConfiguration.FIXED);
		}

		@Bean
		InMemoryGuardianNotifier guardianNotifier() {
			return new InMemoryGuardianNotifier();
		}
	}

	@Autowired
	ConsentService consent;

	@Autowired
	GuardianConsentRepository consents;

	@Autowired
	InMemoryGuardianNotifier notifier;

	@Autowired
	FakeClock clock;

	@Test
	void aMinorRegisteringRaisesAConsentRequestAndSendsTheLink(Scenario scenario) {
		UUID userId = UUID.randomUUID();

		scenario.publish(new MinorMenteeRegistered(userId, "teen@example.com", "Pat Guardian", "guardian@example.com"))
				.andWaitForStateChange(() -> notifier.sent().stream().findFirst())
				.andVerify(sent -> {
					assertThat(sent).isPresent();
					assertThat(sent.get().guardianEmail()).isEqualTo("guardian@example.com");

					UUID token = tokenFrom(sent.get().consentLink());
					ConsentContext context = consent.resolve(token);
					assertThat(context.menteeEmail()).isEqualTo("teen@example.com");
					assertThat(context.guardianName()).isEqualTo("Pat Guardian");
					assertThat(context.state()).isEqualTo(ConsentLinkState.PENDING);
				});
	}

	@Test
	void consentingRecordsItAndAnnouncesItToIdentity(Scenario scenario) {
		UUID userId = UUID.randomUUID();
		UUID token = seedPendingRequest(userId);

		scenario.stimulate(() -> consent.submit(token, new ConsentSubmission(true, true)))
				.andWaitForEventOfType(GuardianConsentGranted.class)
				.matchingMappedValue(GuardianConsentGranted::userId, userId)
				.toArriveAndVerify(event -> assertThat(event.consentedAt()).isEqualTo(FixedClockConfiguration.FIXED));

		assertThat(consent.resolve(token).state()).isEqualTo(ConsentLinkState.CONSENTED);
	}

	@Test
	void aReusedLinkIsRefused() {
		UUID token = seedPendingRequest(UUID.randomUUID());
		consent.submit(token, new ConsentSubmission(true, true));

		assertThatExceptionOfType(ConsentLinkAlreadyUsedException.class)
				.isThrownBy(() -> consent.submit(token, new ConsentSubmission(true, true)));
	}

	@Test
	void anExpiredLinkIsRefused() {
		UUID token = seedPendingRequest(UUID.randomUUID());
		clock.set(FixedClockConfiguration.FIXED.plus(Duration.ofDays(8)));

		assertThatExceptionOfType(ConsentLinkExpiredException.class)
				.isThrownBy(() -> consent.submit(token, new ConsentSubmission(true, true)));
		assertThat(consent.resolve(token).state()).isEqualTo(ConsentLinkState.EXPIRED);
	}

	@Test
	void anUnaffirmedSubmissionIsRefused() {
		UUID token = seedPendingRequest(UUID.randomUUID());

		assertThatExceptionOfType(ConsentNotAffirmedException.class)
				.isThrownBy(() -> consent.submit(token, new ConsentSubmission(true, false)));
	}

	@Test
	void anUnknownLinkIsRefused() {
		assertThatExceptionOfType(ConsentLinkNotFoundException.class)
				.isThrownBy(() -> consent.resolve(UUID.randomUUID()));
		assertThatExceptionOfType(ConsentLinkNotFoundException.class)
				.isThrownBy(() -> consent.submit(UUID.randomUUID(), new ConsentSubmission(true, true)));
	}

	private UUID seedPendingRequest(UUID userId) {
		Instant now = FixedClockConfiguration.FIXED;
		UUID token = UUID.randomUUID();
		consents.save(new GuardianConsent(token, userId, "teen@example.com", "Pat Guardian", "guardian@example.com",
				now, now.plus(Duration.ofDays(7))));
		return token;
	}

	private static UUID tokenFrom(URI consentLink) {
		String path = consentLink.getPath();
		return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
	}
}
