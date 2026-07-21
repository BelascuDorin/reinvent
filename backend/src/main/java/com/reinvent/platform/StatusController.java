package com.reinvent.platform;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal end-to-end endpoint that proves the skeleton walks: the Nuxt app
 * fetches it server-side and renders the result. Deliberately reads time via the
 * {@link Clock} port rather than the wall clock.
 */
@RestController
class StatusController {

	private final Clock clock;

	StatusController(Clock clock) {
		this.clock = clock;
	}

	@GetMapping("/api/status")
	StatusResponse status() {
		return new StatusResponse("reinvent", "ok", clock.now());
	}

	record StatusResponse(String service, String status, Instant time) {
	}
}
