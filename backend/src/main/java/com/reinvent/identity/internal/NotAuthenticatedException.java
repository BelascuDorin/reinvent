package com.reinvent.identity.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when an action needs a signed-in User but there is no active session. */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
class NotAuthenticatedException extends RuntimeException {

	NotAuthenticatedException() {
		super("Not signed in");
	}
}
