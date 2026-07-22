package com.reinvent.identity.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.reinvent.identity.Role;

/** Raised when a signed-in User lacks the role an action requires. */
@ResponseStatus(HttpStatus.FORBIDDEN)
class AccessDeniedException extends RuntimeException {

	AccessDeniedException(Role required) {
		super("Requires the " + required + " role");
	}
}
