package com.reinvent.mentorship.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when there is no Mentor profile to return: either the caller has none of their
 * own (they were never approved as a Mentor), or the Mentor a visitor asked for is not
 * publicly available. The public case says nothing about why — an unknown Mentor, an
 * unfinished profile, and a suspended one look alike from outside, so a visitor can't
 * use it to learn who is suspended.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class MentorProfileNotFoundException extends RuntimeException {

	MentorProfileNotFoundException() {
		super("You do not have a Mentor profile.");
	}

	private MentorProfileNotFoundException(String message) {
		super(message);
	}

	static MentorProfileNotFoundException notPubliclyAvailable() {
		return new MentorProfileNotFoundException("No Mentor profile is available.");
	}
}
