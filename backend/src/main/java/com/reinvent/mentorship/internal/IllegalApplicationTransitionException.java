package com.reinvent.mentorship.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Raised when a Reviewer attempts an illegal Mentor-application state transition. */
@ResponseStatus(HttpStatus.CONFLICT)
class IllegalApplicationTransitionException extends RuntimeException {

	IllegalApplicationTransitionException(String message) {
		super(message);
	}
}
