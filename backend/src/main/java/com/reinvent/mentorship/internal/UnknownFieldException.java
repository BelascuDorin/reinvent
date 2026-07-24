package com.reinvent.mentorship.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when a profile update references a Field slug outside the curated set,
 * keeping discovery's primary axis clean (spec 0002: a Field reference outside the
 * curated list is rejected).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class UnknownFieldException extends RuntimeException {

	UnknownFieldException() {
		super("One or more of the chosen Fields is not a curated Field.");
	}
}
