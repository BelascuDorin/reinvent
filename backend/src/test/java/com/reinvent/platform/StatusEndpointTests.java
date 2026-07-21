package com.reinvent.platform;

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
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Drives the status endpoint through the HTTP boundary. The shared fixed
 * {@link Clock} pins the time the endpoint reports, proving it reads the Clock
 * port rather than the wall clock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, FixedClockConfiguration.class })
class StatusEndpointTests {

	@Autowired
	MockMvc mvc;

	@Test
	void reportsOkStatusWithTimeFromTheClockPort() throws Exception {
		mvc.perform(get("/api/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("reinvent"))
				.andExpect(jsonPath("$.status").value("ok"))
				.andExpect(jsonPath("$.time").value(FixedClockConfiguration.FIXED.toString()));
	}
}
