package com.reinvent.mentorship.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when a User has no Mentor profile (they were never approved as a Mentor). */
@ResponseStatus(HttpStatus.NOT_FOUND)
class MentorProfileNotFoundException extends RuntimeException {

	MentorProfileNotFoundException() {
		super("You do not have a Mentor profile.");
	}
}
