package com.reinvent.mentorship.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when no Mentor application matches the given id. */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ApplicationNotFoundException extends RuntimeException {

	ApplicationNotFoundException() {
		super("No such Mentor application.");
	}
}
