package com.reinvent.mentorship.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when a User already has an open or approved Mentor application. */
@ResponseStatus(HttpStatus.CONFLICT)
class DuplicateApplicationException extends RuntimeException {

	DuplicateApplicationException() {
		super("You already have an open or approved Mentor application.");
	}
}
