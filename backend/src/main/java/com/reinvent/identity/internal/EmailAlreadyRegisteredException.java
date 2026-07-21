package com.reinvent.identity.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when a signup uses an email that already has an account. */
@ResponseStatus(HttpStatus.CONFLICT)
class EmailAlreadyRegisteredException extends RuntimeException {

	EmailAlreadyRegisteredException(String email) {
		super("An account already exists for " + email);
	}
}
