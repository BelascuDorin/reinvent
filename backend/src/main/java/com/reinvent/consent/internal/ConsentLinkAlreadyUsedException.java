package com.reinvent.consent.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when a consent link has already been consented — it is single-use. */
@ResponseStatus(HttpStatus.CONFLICT)
class ConsentLinkAlreadyUsedException extends RuntimeException {

	ConsentLinkAlreadyUsedException() {
		super("This consent link has already been used.");
	}
}
