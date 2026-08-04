package com.reinvent.mentorship;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

/**
 * The actor stories the mentorship HTTP tests share: a User signs up, applies to be a
 * Mentor, the seeded Reviewer works the queue, and a Mentor fills in their profile.
 * Every step drives the same public HTTP boundary the tests assert on — no shortcuts
 * into repositories or services — so an arranged scenario is only reachable in ways a
 * real actor could reach it.
 */
class MentorshipScenarios {

	static final String REVIEWER_EMAIL = "reviewer@reinvent.example";
	static final String REVIEWER_PASSWORD = "reviewer-dev-password";

	/** The MENTOR role is granted asynchronously on MentorApproved, so approval waits. */
	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	private final MockMvc mvc;

	MentorshipScenarios(MockMvc mvc) {
		this.mvc = mvc;
	}

	/**
	 * An approved Mentor: their session for acting as themselves, and their application
	 * id for the Reviewer actions (suspension) that are keyed by it.
	 */
	record ApprovedMentor(MockHttpSession session, String applicationId) {
	}

	/** Signs up, applies, and has the seeded Reviewer approve — MENTOR role granted. */
	ApprovedMentor approvedMentor(String email) throws Exception {
		MockHttpSession applicant = signUpAndLogIn(email);
		String applicationId = applyAndReturnId(applicant);

		MockHttpSession reviewer = reviewer();
		mvc.perform(post("/api/reviewer/applications/{id}/start-review", applicationId).session(reviewer))
				.andExpect(status().isOk());
		mvc.perform(post("/api/reviewer/applications/{id}/approve", applicationId).session(reviewer))
				.andExpect(status().isOk());

		// Only once the role is granted is the Mentor's own profile endpoint reachable,
		// which makes it the honest signal that approval has fully landed.
		await().atMost(TIMEOUT).untilAsserted(() -> mvc.perform(get("/api/mentor-profiles/me").session(applicant))
				.andExpect(status().isOk()));
		return new ApprovedMentor(applicant, applicationId);
	}

	/** The Mentor saves their profile, exactly as the editor does. */
	void saveProfile(MockHttpSession mentor, String body) throws Exception {
		mvc.perform(put("/api/mentor-profiles/me").session(mentor)
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk());
	}

	/**
	 * The Mentor's own User id — the stable identifier their public profile lives under.
	 * Read from their own profile, which is the only place a Mentor learns it.
	 */
	String mentorUserId(MockHttpSession mentor) throws Exception {
		MvcResult result = mvc.perform(get("/api/mentor-profiles/me").session(mentor))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.mentorUserId");
	}

	/** The Reviewer takes an approved Mentor out of circulation. */
	void suspend(String applicationId) throws Exception {
		mvc.perform(post("/api/reviewer/applications/{id}/suspend", applicationId).session(reviewer()))
				.andExpect(status().isOk());
	}

	/** The Reviewer lifts a suspension, putting the Mentor back in circulation. */
	void reinstate(String applicationId) throws Exception {
		mvc.perform(post("/api/reviewer/applications/{id}/reinstate", applicationId).session(reviewer()))
				.andExpect(status().isOk());
	}

	String applyAndReturnId(MockHttpSession session) throws Exception {
		MvcResult result = mvc.perform(post("/api/mentor-applications").session(session))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
	}

	MockHttpSession reviewer() throws Exception {
		return logIn(REVIEWER_EMAIL, REVIEWER_PASSWORD);
	}

	MockHttpSession signUpAndLogIn(String email) throws Exception {
		mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"password1","dateOfBirth":"1990-01-01"}
						""".formatted(email)))
				.andExpect(status().isCreated());
		return logIn(email, "password1");
	}

	MockHttpSession logIn(String email, String password) throws Exception {
		MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"%s"}
						""".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}
}
