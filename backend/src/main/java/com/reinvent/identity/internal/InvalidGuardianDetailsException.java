package com.reinvent.identity.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when the Guardian details on a signup don't match the applicant's age: a
 * minor must name a Guardian (name + email) and an adult must not name one at all
 * (ADR-0002).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidGuardianDetailsException extends RuntimeException {

	InvalidGuardianDetailsException(String message) {
		super(message);
	}
}
