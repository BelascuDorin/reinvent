package com.reinvent.mentorship;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

	/** A fully filled-in, discoverable profile (≥1 Field, price, Meeting duration). */
	private static final String COMPLETE_PROFILE = """
			{"displayName":"Ada Lovelace","roleTitle":"Engineer","bio":"I build things.",
			 "experience":"10 years","employer":"Analytical Engines","priceAmount":120.00,
			 "priceCurrency":"USD","meetingDurationMinutes":45,
			 "fieldSlugs":["software-engineering"],"languages":["en"]}
			""";

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

	@Test
	void updatingTheProfileRequiresTheMentorRole() throws Exception {
		mvc.perform(put("/api/mentor-profiles/me").contentType(MediaType.APPLICATION_JSON).content(COMPLETE_PROFILE))
				.andExpect(status().isUnauthorized());

		MockHttpSession mentee = signUpAndLogIn("mentee-cannot-edit@example.com");
		mvc.perform(put("/api/mentor-profiles/me").session(mentee)
				.contentType(MediaType.APPLICATION_JSON).content(COMPLETE_PROFILE))
				.andExpect(status().isForbidden());
	}

	@Test
	void completingTheDraftMakesTheMentorDiscoverableAndClearingItRemovesThem() throws Exception {
		MockHttpSession mentor = approvedMentor("completes-profile@example.com");

		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content(COMPLETE_PROFILE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("Ada Lovelace"))
				.andExpect(jsonPath("$.employer").value("Analytical Engines"))
				.andExpect(jsonPath("$.fieldSlugs", org.hamcrest.Matchers.contains("software-engineering")))
				.andExpect(jsonPath("$.languages", org.hamcrest.Matchers.contains("en")))
				.andExpect(jsonPath("$.priceAmount").value(120.00))
				.andExpect(jsonPath("$.meetingDurationMinutes").value(45))
				.andExpect(jsonPath("$.complete").value(true))
				.andExpect(jsonPath("$.discoverable").value(true));

		// Clearing a required field (here: all Fields) drops discoverability without
		// destroying the rest of the profile data.
		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Ada Lovelace","roleTitle":"Engineer","bio":"","experience":"",
						 "employer":"Analytical Engines","priceAmount":120.00,"priceCurrency":"USD",
						 "meetingDurationMinutes":45,"fieldSlugs":[],"languages":["en"]}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("Ada Lovelace"))
				.andExpect(jsonPath("$.fieldSlugs").isEmpty())
				.andExpect(jsonPath("$.complete").value(false))
				.andExpect(jsonPath("$.discoverable").value(false));
	}

	@Test
	void anEmptyEmployerIsAccepted() throws Exception {
		MockHttpSession mentor = approvedMentor("blank-employer@example.com");

		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Grace Hopper","roleTitle":"Rear Admiral","bio":"","experience":"",
						 "employer":"  ","priceAmount":90.00,"priceCurrency":"USD",
						 "meetingDurationMinutes":30,"fieldSlugs":["software-engineering"],"languages":["en"]}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.employer").doesNotExist())
				.andExpect(jsonPath("$.complete").value(true));
	}

	@Test
	void aFieldOutsideTheCuratedSetIsRejected() throws Exception {
		MockHttpSession mentor = approvedMentor("bad-field@example.com");

		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Alan Turing","roleTitle":"Engineer","bio":"","experience":"",
						 "employer":"","priceAmount":100.00,"priceCurrency":"USD",
						 "meetingDurationMinutes":60,"fieldSlugs":["underwater-basket-weaving"],"languages":["en"]}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void editsAreScopedToTheCallersOwnProfile() throws Exception {
		MockHttpSession first = approvedMentor("owner-one@example.com");
		MockHttpSession second = approvedMentor("owner-two@example.com");

		mvc.perform(put("/api/mentor-profiles/me").session(first)
				.contentType(MediaType.APPLICATION_JSON).content(COMPLETE_PROFILE))
				.andExpect(status().isOk());

		// The second Mentor's profile is untouched by the first's edit — /me writes
		// only ever hit the caller's own id, so one Mentor can't alter another's.
		mvc.perform(get("/api/mentor-profiles/me").session(second))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").doesNotExist())
				.andExpect(jsonPath("$.complete").value(false));
	}

	/** Signs up, applies, and has the seeded Reviewer approve — returning the Mentor's
	 * session once the MENTOR role has been granted asynchronously. */
	private MockHttpSession approvedMentor(String email) throws Exception {
		MockHttpSession applicant = signUpAndLogIn(email);
		String applicationId = applyAndReturnId(applicant);

		MockHttpSession reviewer = logIn(REVIEWER_EMAIL, REVIEWER_PASSWORD);
		mvc.perform(post("/api/reviewer/applications/{id}/start-review", applicationId).session(reviewer))
				.andExpect(status().isOk());
		mvc.perform(post("/api/reviewer/applications/{id}/approve", applicationId).session(reviewer))
				.andExpect(status().isOk());

		await().atMost(TIMEOUT).untilAsserted(() -> mvc.perform(get("/api/mentor-profiles/me").session(applicant))
				.andExpect(status().isOk()));
		return applicant;
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
