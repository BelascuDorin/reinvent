package com.reinvent.identity.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised on a login attempt whose email/password do not match. */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidCredentialsException extends RuntimeException {

	InvalidCredentialsException() {
		super("Invalid email or password");
	}
}
