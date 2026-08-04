package com.reinvent.mentorship;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

	/** A fully filled-in, discoverable profile (≥1 Field, price, Meeting duration). */
	private static final String COMPLETE_PROFILE = """
			{"displayName":"Ada Lovelace","roleTitle":"Engineer","bio":"I build things.",
			 "experience":"10 years","employer":"Analytical Engines","priceAmount":120.00,
			 "priceCurrency":"USD","meetingDurationMinutes":45,
			 "fieldSlugs":["software-engineering"],"languages":["en"]}
			""";

	@Autowired
	MockMvc mvc;

	private MentorshipScenarios scenarios;

	@BeforeEach
	void setUp() {
		scenarios = new MentorshipScenarios(mvc);
	}

	@Test
	void readingTheProfileRequiresTheMentorRole() throws Exception {
		mvc.perform(get("/api/mentor-profiles/me")).andExpect(status().isUnauthorized());

		MockHttpSession mentee = scenarios.signUpAndLogIn("just-a-mentee-profile@example.com");
		mvc.perform(get("/api/mentor-profiles/me").session(mentee)).andExpect(status().isForbidden());
	}

	@Test
	void anApprovedMentorReadsTheirDraftProfileWhichIsNotYetDiscoverable() throws Exception {
		MockHttpSession mentor = scenarios.approvedMentor("reads-own-profile@example.com").session();

		mvc.perform(get("/api/mentor-profiles/me").session(mentor))
				.andExpect(status().isOk())
				// The profile returned is the caller's own (the /me endpoint is scoped to
				// their session id), so a Mentor can only ever read their own.
				.andExpect(jsonPath("$.mentorUserId").exists())
				.andExpect(jsonPath("$.complete").value(false))
				.andExpect(jsonPath("$.discoverable").value(false))
				.andExpect(jsonPath("$.fieldSlugs").isEmpty())
				.andExpect(jsonPath("$.priceAmount").doesNotExist())
				.andExpect(jsonPath("$.meetingDurationMinutes").doesNotExist());
	}

	@Test
	void updatingTheProfileRequiresTheMentorRole() throws Exception {
		mvc.perform(put("/api/mentor-profiles/me").contentType(MediaType.APPLICATION_JSON).content(COMPLETE_PROFILE))
				.andExpect(status().isUnauthorized());

		MockHttpSession mentee = scenarios.signUpAndLogIn("mentee-cannot-edit@example.com");
		mvc.perform(put("/api/mentor-profiles/me").session(mentee)
				.contentType(MediaType.APPLICATION_JSON).content(COMPLETE_PROFILE))
				.andExpect(status().isForbidden());
	}

	@Test
	void completingTheDraftMakesTheMentorDiscoverableAndClearingItRemovesThem() throws Exception {
		MockHttpSession mentor = scenarios.approvedMentor("completes-profile@example.com").session();

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
		// destroying the rest of the profile data — every other field the Mentor filled
		// in is still there afterwards, so completing again costs them no retyping.
		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Ada Lovelace","roleTitle":"Engineer","bio":"I build things.",
						 "experience":"10 years","employer":"Analytical Engines","priceAmount":120.00,
						 "priceCurrency":"USD","meetingDurationMinutes":45,"fieldSlugs":[],
						 "languages":["en"]}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fieldSlugs").isEmpty())
				.andExpect(jsonPath("$.complete").value(false))
				.andExpect(jsonPath("$.discoverable").value(false))
				.andExpect(jsonPath("$.displayName").value("Ada Lovelace"))
				.andExpect(jsonPath("$.roleTitle").value("Engineer"))
				.andExpect(jsonPath("$.bio").value("I build things."))
				.andExpect(jsonPath("$.experience").value("10 years"))
				.andExpect(jsonPath("$.employer").value("Analytical Engines"))
				.andExpect(jsonPath("$.priceAmount").value(120.00))
				.andExpect(jsonPath("$.priceCurrency").value("USD"))
				.andExpect(jsonPath("$.meetingDurationMinutes").value(45))
				.andExpect(jsonPath("$.languages", org.hamcrest.Matchers.contains("en")));
	}

	@Test
	void anEmptyEmployerIsAccepted() throws Exception {
		MockHttpSession mentor = scenarios.approvedMentor("blank-employer@example.com").session();

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
		MockHttpSession mentor = scenarios.approvedMentor("bad-field@example.com").session();

		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Alan Turing","roleTitle":"Engineer","bio":"","experience":"",
						 "employer":"","priceAmount":100.00,"priceCurrency":"USD",
						 "meetingDurationMinutes":60,"fieldSlugs":["underwater-basket-weaving"],"languages":["en"]}
						"""))
				.andExpect(status().isBadRequest());

		// A null slug names no curated Field either, so it is refused the same way
		// rather than blowing up on the way to the vocabulary.
		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Alan Turing","roleTitle":"Engineer","bio":"","experience":"",
						 "employer":"","priceAmount":100.00,"priceCurrency":"USD",
						 "meetingDurationMinutes":60,"fieldSlugs":[null],"languages":["en"]}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void anAbsurdPriceOrMeetingDurationIsRefused() throws Exception {
		MockHttpSession mentor = scenarios.approvedMentor("absurd-quantities@example.com").session();

		// A zero-minute Meeting and a negative price would otherwise count as "filled
		// in" and make the Mentor discoverable on nonsense terms.
		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Ada Lovelace","roleTitle":"Engineer","bio":"","experience":"",
						 "employer":"","priceAmount":120.00,"priceCurrency":"USD",
						 "meetingDurationMinutes":0,"fieldSlugs":["software-engineering"],"languages":["en"]}
						"""))
				.andExpect(status().isBadRequest());

		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"displayName":"Ada Lovelace","roleTitle":"Engineer","bio":"","experience":"",
						 "employer":"","priceAmount":-1.00,"priceCurrency":"USD",
						 "meetingDurationMinutes":45,"fieldSlugs":["software-engineering"],"languages":["en"]}
						"""))
				.andExpect(status().isBadRequest());

		// Neither attempt touched the draft.
		mvc.perform(get("/api/mentor-profiles/me").session(mentor))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.complete").value(false));
	}

	@Test
	void editsAreScopedToTheCallersOwnProfile() throws Exception {
		MockHttpSession first = scenarios.approvedMentor("owner-one@example.com").session();
		MockHttpSession second = scenarios.approvedMentor("owner-two@example.com").session();

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

}
