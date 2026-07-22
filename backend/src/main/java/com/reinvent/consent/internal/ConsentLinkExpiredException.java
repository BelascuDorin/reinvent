package com.reinvent.consent.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when a consent link's validity window has lapsed; a fresh link is needed. */
@ResponseStatus(HttpStatus.GONE)
class ConsentLinkExpiredException extends RuntimeException {

	ConsentLinkExpiredException() {
		super("This consent link has expired.");
	}
}
