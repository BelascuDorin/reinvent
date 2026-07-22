package com.reinvent.consent.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when a consent submission does not affirm both guardianship and agreement
 * to Reinvent's use — consent is a single action confirming both (ADR-0002).
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class ConsentNotAffirmedException extends RuntimeException {

	ConsentNotAffirmedException() {
		super("Consent requires confirming guardianship and agreement.");
	}
}
