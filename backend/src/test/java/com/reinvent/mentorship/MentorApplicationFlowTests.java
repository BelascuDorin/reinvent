package com.reinvent.mentorship;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
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

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.platform.PaymentGateway;
import com.reinvent.platform.testing.InMemoryPaymentGateway;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Drives the Mentor-application and Reviewer flow end-to-end through the HTTP
 * boundary across the mentorship and identity modules: an applicant applies, the
 * seeded Reviewer works the queue, approval grants the MENTOR role (asynchronously,
 * via the MentorApproved event), and the REVIEWER gate refuses everyone else. Only
 * the ports are doubled (payments, clock); Postgres is real, and the first Reviewer
 * comes from the V7 seed migration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class MentorApplicationFlowTests {

	@TestConfiguration(proxyBeanMethods = false)
	static class Payments {

		@Bean
		@Primary
		PaymentGateway paymentGateway() {
			return new InMemoryPaymentGateway();
		}
	}

	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	@Autowired
	MockMvc mvc;

	private MentorshipScenarios scenarios;

	@BeforeEach
	void setUp() {
		scenarios = new MentorshipScenarios(mvc);
	}

	@Test
	void anApplicantAppliesAndSeesTheirStatus() throws Exception {
		MockHttpSession session = scenarios.signUpAndLogIn("applicant-status@example.com");

		mvc.perform(post("/api/mentor-applications").session(session))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("APPLIED"))
				.andExpect(jsonPath("$.bookable").value(false));

		mvc.perform(get("/api/mentor-applications/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPLIED"));
	}

	@Test
	void applyingRequiresBeingSignedIn() throws Exception {
		mvc.perform(post("/api/mentor-applications")).andExpect(status().isUnauthorized());
	}

	@Test
	void aUserCannotApplyTwiceWhileOpen() throws Exception {
		MockHttpSession session = scenarios.signUpAndLogIn("applicant-dup@example.com");
		mvc.perform(post("/api/mentor-applications").session(session)).andExpect(status().isCreated());

		mvc.perform(post("/api/mentor-applications").session(session)).andExpect(status().isConflict());
	}

	@Test
	void reviewerActionsAreRefusedToNonReviewers() throws Exception {
		MockHttpSession mentee = scenarios.signUpAndLogIn("just-a-mentee@example.com");

		mvc.perform(get("/api/reviewer/applications").session(mentee)).andExpect(status().isForbidden());
		mvc.perform(get("/api/reviewer/applications")).andExpect(status().isUnauthorized());
	}

	@Test
	void reviewerApprovesAndTheApplicantGainsTheMentorRole() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("becomes-mentor@example.com");
		String applicationId = scenarios.applyAndReturnId(applicant);

		MockHttpSession reviewer = scenarios.reviewer();
		mvc.perform(get("/api/reviewer/applications").session(reviewer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == '%s')]".formatted(applicationId)).exists());

		mvc.perform(post("/api/reviewer/applications/{id}/start-review", applicationId).session(reviewer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
		mvc.perform(post("/api/reviewer/applications/{id}/approve", applicationId).session(reviewer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.bookable").value(false)); // approved, but not payment-onboarded

		// identity grants the MENTOR role asynchronously on MentorApproved.
		await().atMost(TIMEOUT).untilAsserted(() -> mvc.perform(get("/api/accounts/me").session(applicant))
				.andExpect(jsonPath("$.roles", org.hamcrest.Matchers.hasItem("MENTOR"))));

		mvc.perform(get("/api/mentor-applications/me").session(applicant))
				.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	@Test
	void reviewerRejectsWithAReason() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("gets-rejected@example.com");
		String applicationId = scenarios.applyAndReturnId(applicant);

		MockHttpSession reviewer = scenarios.reviewer();
		mvc.perform(post("/api/reviewer/applications/{id}/start-review", applicationId).session(reviewer))
				.andExpect(status().isOk());
		mvc.perform(post("/api/reviewer/applications/{id}/reject", applicationId).session(reviewer)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"reason":"Not enough detail in the application."}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"));

		mvc.perform(get("/api/mentor-applications/me").session(applicant))
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.rejectionReason").value("Not enough detail in the application."));
	}

	@Test
	void reviewerSuspendsAnApprovedMentor() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("gets-suspended@example.com");
		String applicationId = scenarios.applyAndReturnId(applicant);

		MockHttpSession reviewer = scenarios.reviewer();
		mvc.perform(post("/api/reviewer/applications/{id}/start-review", applicationId).session(reviewer));
		mvc.perform(post("/api/reviewer/applications/{id}/approve", applicationId).session(reviewer));

		// The approved Mentor is reachable in the Reviewer's approved-Mentors list.
		mvc.perform(get("/api/reviewer/applications/mentors").session(reviewer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == '%s')]".formatted(applicationId)).exists());

		mvc.perform(post("/api/reviewer/applications/{id}/suspend", applicationId).session(reviewer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.suspended").value(true))
				.andExpect(jsonPath("$.bookable").value(false));
	}

}
