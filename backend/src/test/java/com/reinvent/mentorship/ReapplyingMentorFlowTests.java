package com.reinvent.mentorship;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * A User who was rejected, reapplied, and was then approved is a Mentor like any other —
 * everywhere. Rejected applications are kept as history, so such a User has several
 * application rows, and every question about "where do they stand" has to read the
 * current one rather than an arbitrary old one.
 *
 * <p>These tests deliberately run under the fixed clock the rest of the suite uses,
 * which stamps every application in a run with the same instant. That makes the
 * timestamp useless as a tiebreaker and is exactly the condition under which reading
 * "the latest by created_at" picks an arbitrary row.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class ReapplyingMentorFlowTests {

	@TestConfiguration(proxyBeanMethods = false)
	static class Payments {

		@Bean
		@Primary
		PaymentGateway paymentGateway() {
			return new InMemoryPaymentGateway();
		}
	}

	@Autowired
	MockMvc mvc;

	private MentorshipScenarios scenarios;

	@BeforeEach
	void setUp() {
		scenarios = new MentorshipScenarios(mvc);
	}

	@Test
	void aRejectedUserWhoReappliesAndIsApprovedBecomesADiscoverableMentor() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("reapplies-then-approved@example.com");
		MockHttpSession reviewer = scenarios.reviewer();

		String rejected = scenarios.applyAndReturnId(applicant);
		mvc.perform(post("/api/reviewer/applications/{id}/start-review", rejected).session(reviewer))
				.andExpect(status().isOk());
		mvc.perform(post("/api/reviewer/applications/{id}/reject", rejected).session(reviewer)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"reason":"Not enough detail the first time."}
						"""))
				.andExpect(status().isOk());

		// The rejection row stays as history; this second application is the current one.
		String approved = scenarios.applyAndReturnId(applicant);
		mvc.perform(post("/api/reviewer/applications/{id}/start-review", approved).session(reviewer))
				.andExpect(status().isOk());
		mvc.perform(post("/api/reviewer/applications/{id}/approve", approved).session(reviewer))
				.andExpect(status().isOk());

		// What the applicant is told about themselves is the new application, not the old
		// rejection — no stale "REJECTED" and no stale reason.
		mvc.perform(get("/api/mentor-applications/me").session(applicant))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(approved))
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.rejectionReason").doesNotExist());

		// And they are a Mentor for real: the profile approval provisioned is reachable,
		// completing it makes them discoverable, and the public sees them.
		mvc.perform(put("/api/mentor-profiles/me").session(applicant)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Second Time Lucky","roleTitle":"Advocate","bio":"Kept going.",
						 "experience":"Plenty","employer":"","priceAmount":80.00,"priceCurrency":"USD",
						 "meetingDurationMinutes":30,"fieldSlugs":["law"],"languages":["en"]}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.discoverable").value(true));

		mvc.perform(get("/api/mentors").param("field", "law"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.displayName == 'Second Time Lucky')]").exists());
	}

	@Test
	void aUserRejectedTwiceStillSeesTheirMostRecentRejection() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("rejected-twice@example.com");
		MockHttpSession reviewer = scenarios.reviewer();

		rejectFreshApplication(applicant, reviewer, "First reason.");
		rejectFreshApplication(applicant, reviewer, "Second reason.");

		// Two rejections, same timestamp: the answer must still be one specific
		// application rather than whichever row the database happened to return.
		mvc.perform(get("/api/mentor-applications/me").session(applicant))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.rejectionReason").value("Second reason."));
	}

	private void rejectFreshApplication(MockHttpSession applicant, MockHttpSession reviewer, String reason)
			throws Exception {
		String applicationId = scenarios.applyAndReturnId(applicant);
		mvc.perform(post("/api/reviewer/applications/{id}/start-review", applicationId).session(reviewer))
				.andExpect(status().isOk());
		mvc.perform(post("/api/reviewer/applications/{id}/reject", applicationId).session(reviewer)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"reason":"%s"}
						""".formatted(reason)))
				.andExpect(status().isOk());
	}
}
