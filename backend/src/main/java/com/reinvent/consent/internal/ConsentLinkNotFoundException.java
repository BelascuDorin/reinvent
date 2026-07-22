package com.reinvent.consent.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when a consent link token matches no Guardian consent request. */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ConsentLinkNotFoundException extends RuntimeException {

	ConsentLinkNotFoundException() {
		super("This consent link is not valid.");
	}
}
