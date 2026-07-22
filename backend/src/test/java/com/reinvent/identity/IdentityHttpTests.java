package com.reinvent.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Drives the identity module through its HTTP boundary — the highest seam — and
 * asserts only externally observable behaviour (status codes, response bodies,
 * session-gated access). Fixed clock: "now" is 2026-01-01.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class IdentityHttpTests {

	@Autowired
	MockMvc mvc;

	@Test
	void signupCreatesAMenteeByDefault() throws Exception {
		mvc.perform(signup("adult@example.com", "password1", "1990-01-01"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("adult@example.com"))
				.andExpect(jsonPath("$.roles").value("MENTEE"))
				.andExpect(jsonPath("$.minor").value(false))
				.andExpect(jsonPath("$.guardianConsentStatus").value("NOT_REQUIRED"));
	}

	@Test
	void signupOfAMinorWhoNamesAGuardianIsPendingConsent() throws Exception {
		mvc.perform(signupMinor("minor@example.com", "password1", "2015-01-01", "Pat Guardian",
				"guardian@example.com"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.minor").value(true))
				.andExpect(jsonPath("$.guardianConsentStatus").value("PENDING_GUARDIAN_CONSENT"));
	}

	@Test
	void signupOfAMinorWithoutAGuardianIsRejected() throws Exception {
		mvc.perform(signup("lonely-minor@example.com", "password1", "2015-01-01"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void signupOfAnAdultNamingAGuardianIsRejected() throws Exception {
		mvc.perform(signupMinor("adult-guardian@example.com", "password1", "1990-01-01", "Pat Guardian",
				"guardian@example.com"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void signupRejectsAnEmailThatAlreadyHasAnAccount() throws Exception {
		mvc.perform(signup("dup@example.com", "password1", "1990-01-01")).andExpect(status().isCreated());
		mvc.perform(signup("dup@example.com", "password2", "1985-05-05")).andExpect(status().isConflict());
	}

	@Test
	void loginWithWrongPasswordIsRejected() throws Exception {
		mvc.perform(signup("login-bad@example.com", "password1", "1990-01-01")).andExpect(status().isCreated());
		mvc.perform(login("login-bad@example.com", "wrong-password")).andExpect(status().isUnauthorized());
	}

	@Test
	void currentAccountRequiresASession() throws Exception {
		mvc.perform(get("/api/accounts/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void loginThenReadCurrentAccount() throws Exception {
		mvc.perform(signup("session@example.com", "password1", "1990-01-01")).andExpect(status().isCreated());

		MvcResult loggedIn = mvc.perform(login("session@example.com", "password1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("session@example.com"))
				.andReturn();
		MockHttpSession session = (MockHttpSession) loggedIn.getRequest().getSession(false);

		mvc.perform(get("/api/accounts/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("session@example.com"))
				.andExpect(jsonPath("$.roles").value("MENTEE"));
	}

	@Test
	void logoutEndsTheSession() throws Exception {
		mvc.perform(signup("logout@example.com", "password1", "1990-01-01")).andExpect(status().isCreated());

		MvcResult loggedIn = mvc.perform(login("logout@example.com", "password1"))
				.andExpect(status().isOk()).andReturn();
		MockHttpSession session = (MockHttpSession) loggedIn.getRequest().getSession(false);

		mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isNoContent());

		// The invalidated session no longer grants access.
		mvc.perform(get("/api/accounts/me").session(session)).andExpect(status().isUnauthorized());
	}

	private static org.springframework.test.web.servlet.RequestBuilder signup(String email, String password,
			String dateOfBirth) {
		return post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"%s","dateOfBirth":"%s"}
						""".formatted(email, password, dateOfBirth));
	}

	private static org.springframework.test.web.servlet.RequestBuilder signupMinor(String email, String password,
			String dateOfBirth, String guardianName, String guardianEmail) {
		return post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"%s","dateOfBirth":"%s","guardianName":"%s","guardianEmail":"%s"}
						""".formatted(email, password, dateOfBirth, guardianName, guardianEmail));
	}

	private static org.springframework.test.web.servlet.RequestBuilder login(String email, String password) {
		return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"%s"}
						""".formatted(email, password));
	}
}
