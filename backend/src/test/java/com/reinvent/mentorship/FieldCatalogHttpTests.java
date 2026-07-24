package com.reinvent.mentorship;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.reinvent.TestcontainersConfiguration;

/**
 * The curated Fields are browsable by anyone, signed in or not — discovery starts
 * before an account exists (spec 0002). Drives the public HTTP boundary against a
 * real Postgres seeded by Flyway; no session is presented.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class FieldCatalogHttpTests {

	@Autowired
	MockMvc mvc;

	@Test
	void anyoneCanListTheCuratedFieldsWithoutSigningIn() throws Exception {
		mvc.perform(get("/api/fields"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].slug").value("software-engineering"))
				.andExpect(jsonPath("$[0].displayName").value("Software Engineering"))
				.andExpect(jsonPath("$[?(@.slug == 'medicine')].displayName").value("Medicine"));
	}
}
