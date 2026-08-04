package com.reinvent.mentorship;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * What an applicant writes when they apply, and what becomes of it. A Mentor application
 * carries a headline and a bio: it gives the Reviewer something to actually vet (ADR-0001
 * is a manual approval, not a rubber stamp), and on approval it seeds the Mentor's draft
 * profile so an approved Mentor doesn't face a blank page.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class MentorApplicationContentFlowTests {

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
	void applyingCarriesAHeadlineAndABioTheApplicantCanSeeBack() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("application-content@example.com");

		mvc.perform(post("/api/mentor-applications").session(applicant)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"headline":"Staff Engineer at a hospital","bio":"Fifteen years building clinical software."}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.headline").value("Staff Engineer at a hospital"))
				.andExpect(jsonPath("$.bio").value("Fifteen years building clinical software."));

		mvc.perform(get("/api/mentor-applications/me").session(applicant))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.headline").value("Staff Engineer at a hospital"))
				.andExpect(jsonPath("$.bio").value("Fifteen years building clinical software."));
	}

	@Test
	void anApplicationWithoutAHeadlineOrBioIsRefused() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("empty-application@example.com");

		mvc.perform(post("/api/mentor-applications").session(applicant)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());

		mvc.perform(post("/api/mentor-applications").session(applicant)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"headline":"   ","bio":"   "}
						"""))
				.andExpect(status().isBadRequest());

		// Refused means refused: nothing was recorded, so they still have no application.
		mvc.perform(get("/api/mentor-applications/me").session(applicant))
				.andExpect(status().isNotFound());
	}

	@Test
	void theReviewerSeesWhatTheApplicantWroteInTheQueue() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("reviewer-reads-application@example.com");
		String applicationId = scenarios.applyAndReturnId(applicant, "Barrister, twenty years", "I mostly do crime.");

		mvc.perform(get("/api/reviewer/applications").session(scenarios.reviewer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == '%s')].headline".formatted(applicationId))
						.value("Barrister, twenty years"))
				.andExpect(jsonPath("$[?(@.id == '%s')].bio".formatted(applicationId))
						.value("I mostly do crime."));
	}

	@Test
	void approvalSeedsTheDraftProfileFromTheApplication() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("seeded-draft@example.com");
		scenarios.approveApplication(applicant,
				scenarios.applyAndReturnId(applicant, "Product Manager at a bank", "I ship things people use."));

		// The draft is no longer blank: what they wrote when applying is already there.
		mvc.perform(get("/api/mentor-profiles/me").session(applicant))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roleTitle").value("Product Manager at a bank"))
				.andExpect(jsonPath("$.bio").value("I ship things people use."))
				// Seeding is a head start, not a finished profile: no Field, price or
				// Meeting duration yet, so they are still not discoverable.
				.andExpect(jsonPath("$.complete").value(false))
				.andExpect(jsonPath("$.discoverable").value(false));
	}

	@Test
	void theMentorsOwnEditsReplaceTheWordsTheyAppliedWith() throws Exception {
		MockHttpSession applicant = scenarios.signUpAndLogIn("keeps-own-edits@example.com");
		scenarios.approveApplication(applicant,
				scenarios.applyAndReturnId(applicant, "First headline", "First bio"));

		scenarios.saveProfile(applicant, """
				{"displayName":"Edith Clarke","roleTitle":"Electrical Engineer","bio":"My own words.",
				 "experience":"Decades","employer":"","priceAmount":90.00,"priceCurrency":"USD",
				 "meetingDurationMinutes":30,"fieldSlugs":["software-engineering"],"languages":["en"]}
				""");

		// The seeded headline and bio were a starting point, not a fixture: editing
		// replaces them outright, and nothing re-seeds them afterwards.
		mvc.perform(get("/api/mentor-profiles/me").session(applicant))
				.andExpect(jsonPath("$.roleTitle").value("Electrical Engineer"))
				.andExpect(jsonPath("$.bio").value("My own words."));
	}
}
