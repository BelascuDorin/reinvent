package com.reinvent.consent.internal;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Guardian's boundary into the consent flow. Public by design: a Guardian is
 * not a User and acts only with the link's token, never a session.
 */
@RestController
@RequestMapping("/api/consent")
class ConsentController {

	private final ConsentService consent;

	ConsentController(ConsentService consent) {
		this.consent = consent;
	}

	@GetMapping("/{token}")
	ConsentContext resolve(@PathVariable UUID token) {
		return consent.resolve(token);
	}

	@PostMapping("/{token}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void submit(@PathVariable UUID token, @RequestBody ConsentSubmission submission) {
		consent.submit(token, submission);
	}
}
