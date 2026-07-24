package com.reinvent.mentorship;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

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

import com.jayway.jsonpath.JsonPath;
import com.reinvent.TestcontainersConfiguration;
import com.reinvent.platform.PaymentGateway;
import com.reinvent.platform.testing.InMemoryPaymentGateway;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Drives the owner's Mentor-profile boundary end-to-end through HTTP: only a User with
 * the MENTOR role reaches it, an approved Mentor reads the draft that approval
 * provisioned, and that draft is not yet discoverable. Ports are doubled (payments,
 * clock); Postgres is real and the first Reviewer comes from the V7 seed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class MentorProfileFlowTests {

	@TestConfiguration(proxyBeanMethods = false)
	static class Payments {

		@Bean
		@Primary
		PaymentGateway paymentGateway() {
			return new InMemoryPaymentGateway();
		}
	}

	private static final Duration TIMEOUT = Duration.ofSeconds(10);
	private static final String REVIEWER_EMAIL = "reviewer@reinvent.example";
	private static final String REVIEWER_PASSWORD = "reviewer-dev-password";

	@Autowired
	MockMvc mvc;

	@Test
	void readingTheProfileRequiresTheMentorRole() throws Exception {
		mvc.perform(get("/api/mentor-profiles/me")).andExpect(status().isUnauthorized());

		MockHttpSession mentee = signUpAndLogIn("just-a-mentee-profile@example.com");
		mvc.perform(get("/api/mentor-profiles/me").session(mentee)).andExpect(status().isForbidden());
	}

	@Test
	void anApprovedMentorReadsTheirDraftProfileWhichIsNotYetDiscoverable() throws Exception {
		MockHttpSession applicant = signUpAndLogIn("reads-own-profile@example.com");
		String applicationId = applyAndReturnId(applicant);

		MockHttpSession reviewer = logIn(REVIEWER_EMAIL, REVIEWER_PASSWORD);
		mvc.perform(post("/api/reviewer/applications/{id}/start-review", applicationId).session(reviewer))
				.andExpect(status().isOk());
		mvc.perform(post("/api/reviewer/applications/{id}/approve", applicationId).session(reviewer))
				.andExpect(status().isOk());

		// The MENTOR role is granted asynchronously on MentorApproved; only then is the
		// profile endpoint reachable.
		await().atMost(TIMEOUT).untilAsserted(() -> mvc.perform(get("/api/mentor-profiles/me").session(applicant))
				.andExpect(status().isOk())
				// The profile returned is the caller's own (the /me endpoint is scoped to
				// their session id), so a Mentor can only ever read their own.
				.andExpect(jsonPath("$.mentorUserId").exists())
				.andExpect(jsonPath("$.complete").value(false))
				.andExpect(jsonPath("$.discoverable").value(false))
				.andExpect(jsonPath("$.fieldSlugs").isEmpty())
				.andExpect(jsonPath("$.priceAmount").doesNotExist())
				.andExpect(jsonPath("$.meetingDurationMinutes").doesNotExist()));
	}

	private String applyAndReturnId(MockHttpSession session) throws Exception {
		MvcResult result = mvc.perform(post("/api/mentor-applications").session(session))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
	}

	private MockHttpSession signUpAndLogIn(String email) throws Exception {
		mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"password1","dateOfBirth":"1990-01-01"}
						""".formatted(email)))
				.andExpect(status().isCreated());
		return logIn(email, "password1");
	}

	private MockHttpSession logIn(String email, String password) throws Exception {
		MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"%s"}
						""".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}
}
