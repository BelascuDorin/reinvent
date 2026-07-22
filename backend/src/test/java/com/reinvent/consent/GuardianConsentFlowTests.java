package com.reinvent.consent;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.platform.GuardianNotifier;
import com.reinvent.platform.testing.InMemoryGuardianNotifier;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Drives the whole Guardian-consent flow end-to-end across the identity and consent
 * modules through their HTTP boundaries, proving the event wiring: a minor signs up,
 * the {@link GuardianNotifier} receives a single-use link, the Guardian consents, and
 * the minor's account state flips to {@code CONSENTED}. The only doubles are the
 * ports (fixed clock, capturing notifier); Postgres is real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class GuardianConsentFlowTests {

	@TestConfiguration(proxyBeanMethods = false)
	static class CapturingNotifier {

		@Bean
		InMemoryGuardianNotifier inMemoryGuardianNotifier() {
			return new InMemoryGuardianNotifier();
		}

		@Bean
		@Primary
		GuardianNotifier guardianNotifier(InMemoryGuardianNotifier notifier) {
			return notifier;
		}
	}

	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	@Autowired
	MockMvc mvc;

	@Autowired
	InMemoryGuardianNotifier notifier;

	@Test
	void aMinorWhoseGuardianConsentsBecomesConsented() throws Exception {
		String minorEmail = "teen-consents@example.com";
		mvc.perform(signupMinor(minorEmail, "password1", "2015-01-01", "Pat Guardian", "guardian@example.com"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.guardianConsentStatus").value("PENDING_GUARDIAN_CONSENT"));

		MockHttpSession session = login(minorEmail, "password1");

		UUID token = awaitLinkTokenFor("guardian@example.com");

		mvc.perform(get("/api/consent/{token}", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.menteeEmail").value(minorEmail))
				.andExpect(jsonPath("$.state").value("PENDING"));

		mvc.perform(submit(token, true, true)).andExpect(status().isNoContent());

		// identity lifts the gate asynchronously once it hears GuardianConsentGranted.
		await().atMost(TIMEOUT).untilAsserted(() -> mvc.perform(get("/api/accounts/me").session(session))
				.andExpect(jsonPath("$.guardianConsentStatus").value("CONSENTED")));
	}

	@Test
	void aReusedLinkIsRefusedAtTheBoundary() throws Exception {
		mvc.perform(signupMinor("teen-reuse@example.com", "password1", "2015-01-01", "Pat Guardian",
				"reuse-guardian@example.com")).andExpect(status().isCreated());

		UUID token = awaitLinkTokenFor("reuse-guardian@example.com");
		mvc.perform(submit(token, true, true)).andExpect(status().isNoContent());

		mvc.perform(submit(token, true, true)).andExpect(status().isConflict());
	}

	@Test
	void anUnaffirmedSubmissionIsRefusedAtTheBoundary() throws Exception {
		mvc.perform(signupMinor("teen-unaffirmed@example.com", "password1", "2015-01-01", "Pat Guardian",
				"unaffirmed-guardian@example.com")).andExpect(status().isCreated());

		UUID token = awaitLinkTokenFor("unaffirmed-guardian@example.com");

		mvc.perform(submit(token, true, false)).andExpect(status().isUnprocessableEntity());
	}

	@Test
	void anUnknownLinkIsNotFound() throws Exception {
		mvc.perform(get("/api/consent/{token}", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	private UUID awaitLinkTokenFor(String guardianEmail) {
		await().atMost(TIMEOUT).until(() -> notifier.sent().stream()
				.anyMatch(sent -> sent.guardianEmail().equals(guardianEmail)));
		URI link = notifier.sent().stream()
				.filter(sent -> sent.guardianEmail().equals(guardianEmail))
				.findFirst().orElseThrow().consentLink();
		String path = link.getPath();
		return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
	}

	private MockHttpSession login(String email, String password) throws Exception {
		MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"%s"}
						""".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	private static org.springframework.test.web.servlet.RequestBuilder signupMinor(String email, String password,
			String dateOfBirth, String guardianName, String guardianEmail) {
		return post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"%s","dateOfBirth":"%s","guardianName":"%s","guardianEmail":"%s"}
						""".formatted(email, password, dateOfBirth, guardianName, guardianEmail));
	}

	private static org.springframework.test.web.servlet.RequestBuilder submit(UUID token, boolean confirmGuardian,
			boolean agreeToTerms) {
		return post("/api/consent/{token}", token).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"confirmGuardian":%s,"agreeToTerms":%s}
						""".formatted(confirmGuardian, agreeToTerms));
	}
}
