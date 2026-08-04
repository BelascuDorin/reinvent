package com.reinvent.mentorship;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MockMvc;

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.mentorship.MentorshipScenarios.ApprovedMentor;
import com.reinvent.platform.PaymentGateway;
import com.reinvent.platform.testing.InMemoryPaymentGateway;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Drives public Mentor discovery through the HTTP boundary as a visitor with no account:
 * browsing and searching by Field and language, and seeing only Mentors worth
 * considering — approved, not suspended, and with a complete profile. Payment onboarding
 * is deliberately not consulted, so the doubled PaymentGateway reports nobody onboarded
 * and Mentors are discoverable anyway. Postgres is real (the seeded Field table and the
 * join are exercised as in production) and the first Reviewer comes from the V7 seed.
 *
 * <p>Every test scopes its assertions to its own Field, because the tests share one
 * database and other classes leave Mentors behind.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class MentorDiscoveryFlowTests {

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
	void aVisitorWithNoAccountBrowsesMentorsInAField() throws Exception {
		ApprovedMentor mentor = scenarios.approvedMentor("discovery-visible@example.com");
		scenarios.saveProfile(mentor.session(), profile("Hippocrates of Kos", "GP", "medicine", "el", 80.00, 30));

		// No session: discovery is public.
		mvc.perform(get("/api/mentors").param("field", "medicine"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.displayName == 'Hippocrates of Kos')]").exists())
				// The summary carries what a Mentee needs to choose, including the
				// rating-summary slot the Reviews slice will fill.
				.andExpect(jsonPath("$[?(@.displayName == 'Hippocrates of Kos')].roleTitle")
						.value("GP"))
				.andExpect(jsonPath("$[?(@.displayName == 'Hippocrates of Kos')].priceAmount")
						.value(80.00))
				.andExpect(jsonPath("$[?(@.displayName == 'Hippocrates of Kos')].meetingDurationMinutes")
						.value(30))
				.andExpect(jsonPath("$[?(@.displayName == 'Hippocrates of Kos')].fieldSlugs[0]")
						.value("medicine"))
				.andExpect(jsonPath("$[?(@.displayName == 'Hippocrates of Kos')].languages[0]")
						.value("el"))
				.andExpect(jsonPath("$[0].ratingSummary").value(Matchers.nullValue()));
	}

	@Test
	void anIncompleteProfileIsNotDiscoverableEvenInItsOwnField() throws Exception {
		ApprovedMentor mentor = scenarios.approvedMentor("discovery-incomplete@example.com");
		// A Field but no price and no Meeting duration: not complete, so not worth showing.
		scenarios.saveProfile(mentor.session(), """
				{"displayName":"Half Finished","roleTitle":"Physician","bio":"","experience":"",
				 "employer":"","priceAmount":null,"priceCurrency":"EUR",
				 "meetingDurationMinutes":null,"fieldSlugs":["medicine"],"languages":["el"]}
				""");

		mvc.perform(get("/api/mentors").param("field", "medicine"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.displayName == 'Half Finished')]").doesNotExist());
	}

	@Test
	void mentorsAreSearchableByFieldAndThenByLanguage() throws Exception {
		ApprovedMentor english = scenarios.approvedMentor("discovery-en@example.com");
		scenarios.saveProfile(english.session(), profile("Edward Coke", "Barrister", "law", "en", 200.00, 60));
		ApprovedMentor french = scenarios.approvedMentor("discovery-fr@example.com");
		scenarios.saveProfile(french.session(), profile("Simone Veil", "Magistrate", "law", "fr", 210.00, 60));

		// Field is the primary axis: both Mentors practise law.
		mvc.perform(get("/api/mentors").param("field", "law"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.displayName == 'Edward Coke')]").exists())
				.andExpect(jsonPath("$[?(@.displayName == 'Simone Veil')]").exists());

		// Language narrows it further.
		mvc.perform(get("/api/mentors").param("field", "law").param("language", "fr"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.displayName == 'Simone Veil')]").exists())
				.andExpect(jsonPath("$[?(@.displayName == 'Edward Coke')]").doesNotExist());

		// Language alone, with no Field, is a valid search too.
		mvc.perform(get("/api/mentors").param("language", "fr"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.displayName == 'Simone Veil')]").exists());
	}

	@Test
	void aFieldWithNoDiscoverableMentorsIsAnEmptyListNotAnError() throws Exception {
		mvc.perform(get("/api/mentors").param("field", "entrepreneurship"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void aSuspendedMentorDisappearsFromDiscovery() throws Exception {
		ApprovedMentor mentor = scenarios.approvedMentor("discovery-suspended@example.com");
		scenarios.saveProfile(mentor.session(), profile("Maria Montessori", "Educator", "education", "it", 70.00, 45));

		mvc.perform(get("/api/mentors").param("field", "education"))
				.andExpect(jsonPath("$[?(@.displayName == 'Maria Montessori')]").exists());

		scenarios.suspend(mentor.applicationId());

		mvc.perform(get("/api/mentors").param("field", "education"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.displayName == 'Maria Montessori')]").doesNotExist());
	}

	@Test
	void editingAProfileBackToIncompleteRemovesTheMentorAndCompletingItAgainRestoresThem() throws Exception {
		ApprovedMentor mentor = scenarios.approvedMentor("discovery-round-trip@example.com");
		String complete = profile("Philip Kotler", "Marketer", "marketing", "en", 150.00, 30);
		scenarios.saveProfile(mentor.session(), complete);

		mvc.perform(get("/api/mentors").param("field", "marketing"))
				.andExpect(jsonPath("$[?(@.displayName == 'Philip Kotler')]").exists());

		// Clearing the Meeting duration is enough to make the profile incomplete.
		scenarios.saveProfile(mentor.session(), """
				{"displayName":"Philip Kotler","roleTitle":"Marketer","bio":"","experience":"",
				 "employer":"","priceAmount":150.00,"priceCurrency":"USD",
				 "meetingDurationMinutes":null,"fieldSlugs":["marketing"],"languages":["en"]}
				""");

		mvc.perform(get("/api/mentors").param("field", "marketing"))
				.andExpect(jsonPath("$[?(@.displayName == 'Philip Kotler')]").doesNotExist());

		// Filling it back in restores them, with no publish step.
		scenarios.saveProfile(mentor.session(), complete);

		mvc.perform(get("/api/mentors").param("field", "marketing"))
				.andExpect(jsonPath("$[?(@.displayName == 'Philip Kotler')]").exists());
	}

	@Test
	void resultsComeBackInAStableOrder() throws Exception {
		ApprovedMentor last = scenarios.approvedMentor("discovery-order-z@example.com");
		scenarios.saveProfile(last.session(), profile("Zelda Fitzgerald", "Analyst", "finance", "en", 100.00, 30));
		ApprovedMentor first = scenarios.approvedMentor("discovery-order-a@example.com");
		scenarios.saveProfile(first.session(), profile("Abigail Adams", "Analyst", "finance", "en", 110.00, 30));

		// Ordered by display name, not by when they joined — the later signup comes first.
		mvc.perform(get("/api/mentors").param("field", "finance"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].displayName").value("Abigail Adams"))
				.andExpect(jsonPath("$[1].displayName").value("Zelda Fitzgerald"));
	}

	/** A complete, discoverable profile in one Field and one language. */
	private static String profile(String displayName, String roleTitle, String fieldSlug, String language,
			double price, int minutes) {
		return """
				{"displayName":"%s","roleTitle":"%s","bio":"Here to help.","experience":"Years of it",
				 "employer":"","priceAmount":%s,"priceCurrency":"USD","meetingDurationMinutes":%d,
				 "fieldSlugs":["%s"],"languages":["%s"]}
				""".formatted(displayName, roleTitle, price, minutes, fieldSlug, language);
	}
}
