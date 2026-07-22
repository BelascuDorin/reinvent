package com.reinvent.consent.internal;

/**
 * The Guardian's one action: in a single submission they confirm both that they are
 * the minor's Guardian and that they consent to the minor's use of Reinvent
 * (ADR-0002). {@link ConsentService} refuses the submission unless both are affirmed.
 */
record ConsentSubmission(boolean confirmGuardian, boolean agreeToTerms) {

	boolean isAffirmed() {
		return confirmGuardian && agreeToTerms;
	}
}
