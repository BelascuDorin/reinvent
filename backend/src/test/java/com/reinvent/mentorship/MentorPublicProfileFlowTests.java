package com.reinvent.mentorship;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.mentorship.MentorshipScenarios.ApprovedMentor;
import com.reinvent.platform.PaymentGateway;
import com.reinvent.platform.testing.InMemoryPaymentGateway;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Drives a Mentor's full public profile through HTTP: a visitor with no account opens it
 * at a stable URL and reads the whole presentation, and the Mentor themselves previews
 * what those visitors will see. A profile is public only while the Mentor is
 * discoverable — the same rule that gates the browse list — so an incomplete or
 * suspended Mentor has nothing publicly readable, while their own preview keeps working.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class MentorPublicProfileFlowTests {

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
	void aVisitorWithNoAccountReadsAMentorsWholePresentation() throws Exception {
		ApprovedMentor mentor = scenarios.approvedMentor("public-profile-read@example.com");
		scenarios.saveProfile(mentor.session(), """
				{"displayName":"Marie Curie","roleTitle":"Research Chemist","bio":"Twice a laureate.",
				 "experience":"Decades in the lab","employer":"Sorbonne","priceAmount":140.00,
				 "priceCurrency":"EUR","meetingDurationMinutes":60,
				 "fieldSlugs":["medicine","education"],"languages":["fr","pl"]}
				""");
		String mentorUserId = scenarios.mentorUserId(mentor.session());

		// No session at all: the profile is public.
		mvc.perform(get("/api/mentors/{id}", mentorUserId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mentorUserId").value(mentorUserId))
				.andExpect(jsonPath("$.displayName").value("Marie Curie"))
				.andExpect(jsonPath("$.roleTitle").value("Research Chemist"))
				.andExpect(jsonPath("$.bio").value("Twice a laureate."))
				.andExpect(jsonPath("$.experience").value("Decades in the lab"))
				.andExpect(jsonPath("$.employer").value("Sorbonne"))
				.andExpect(jsonPath("$.priceAmount").value(140.00))
				.andExpect(jsonPath("$.priceCurrency").value("EUR"))
				.andExpect(jsonPath("$.meetingDurationMinutes").value(60))
				.andExpect(jsonPath("$.fieldSlugs", Matchers.contains("education", "medicine")))
				.andExpect(jsonPath("$.languages", Matchers.contains("fr", "pl")))
				// The seam the Reviews slice fills: present, and empty.
				.andExpect(jsonPath("$.ratingSummary").value(Matchers.nullValue()));
	}

	@Test
	void anEmployerTheMentorLeftBlankIsNotShownAtAll() throws Exception {
		ApprovedMentor mentor = scenarios.approvedMentor("public-profile-no-employer@example.com");
		scenarios.saveProfile(mentor.session(), """
				{"displayName":"Ibn Sina","roleTitle":"Physician","bio":"On healing.",
				 "experience":"A lifetime","employer":"   ","priceAmount":60.00,
				 "priceCurrency":"USD","meetingDurationMinutes":30,
				 "fieldSlugs":["medicine"],"languages":["fa"]}
				""");
		String mentorUserId = scenarios.mentorUserId(mentor.session());

		mvc.perform(get("/api/mentors/{id}", mentorUserId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("Ibn Sina"))
				.andExpect(jsonPath("$.employer").doesNotExist());
	}

	@Test
	void aMentorWhoIsNotDiscoverableHasNoPublicProfile() throws Exception {
		ApprovedMentor incomplete = scenarios.approvedMentor("public-profile-incomplete@example.com");
		String incompleteId = scenarios.mentorUserId(incomplete.session());

		// A freshly approved Mentor has an empty draft — nothing to show a visitor yet.
		mvc.perform(get("/api/mentors/{id}", incompleteId)).andExpect(status().isNotFound());

		ApprovedMentor suspended = scenarios.approvedMentor("public-profile-suspended@example.com");
		scenarios.saveProfile(suspended.session(), """
				{"displayName":"Hidden Away","roleTitle":"Tutor","bio":"Here.","experience":"Some",
				 "employer":"","priceAmount":50.00,"priceCurrency":"USD","meetingDurationMinutes":30,
				 "fieldSlugs":["education"],"languages":["en"]}
				""");
		String suspendedId = scenarios.mentorUserId(suspended.session());
		mvc.perform(get("/api/mentors/{id}", suspendedId)).andExpect(status().isOk());

		scenarios.suspend(suspended.applicationId());

		mvc.perform(get("/api/mentors/{id}", suspendedId)).andExpect(status().isNotFound());
	}

	@Test
	void aStrangerIdIsNotFound() throws Exception {
		mvc.perform(get("/api/mentors/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	void aMentorPreviewsWhatMenteesWillSeeEvenBeforeTheyAreDiscoverable() throws Exception {
		ApprovedMentor mentor = scenarios.approvedMentor("public-profile-preview@example.com");

		// Still an empty draft, so not discoverable — but the preview is how a Mentor
		// checks their presentation on the way to being discoverable.
		mvc.perform(get("/api/mentor-profiles/me/preview").session(mentor.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ratingSummary").value(Matchers.nullValue()))
				.andExpect(jsonPath("$.employer").doesNotExist());

		scenarios.saveProfile(mentor.session(), """
				{"displayName":"Preview Me","roleTitle":"Coach","bio":"A bio.","experience":"Plenty",
				 "employer":"Acme","priceAmount":75.00,"priceCurrency":"GBP",
				 "meetingDurationMinutes":45,"fieldSlugs":["design"],"languages":["en"]}
				""");

		// The preview is the same presentation a visitor gets, not the owner's edit view:
		// no completeness bookkeeping leaks into it.
		mvc.perform(get("/api/mentor-profiles/me/preview").session(mentor.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("Preview Me"))
				.andExpect(jsonPath("$.employer").value("Acme"))
				.andExpect(jsonPath("$.priceAmount").value(75.00))
				.andExpect(jsonPath("$.complete").doesNotExist())
				.andExpect(jsonPath("$.discoverable").doesNotExist());
	}

	@Test
	void previewingRequiresBeingThatMentor() throws Exception {
		mvc.perform(get("/api/mentor-profiles/me/preview")).andExpect(status().isUnauthorized());

		MockHttpSession mentee = scenarios.signUpAndLogIn("preview-not-a-mentor@example.com");
		mvc.perform(get("/api/mentor-profiles/me/preview").session(mentee)).andExpect(status().isForbidden());
	}
}
