package com.reinvent.consent.internal;

/**
 * What a consent link resolves to, so the Guardian can make an informed decision:
 * which minor the link is about and what is being consented to. Carries no identity
 * proof (ADR-0002).
 *
 * @param menteeEmail identifies which minor the Guardian is consenting for
 * @param guardianName the name the minor gave for their Guardian
 * @param purpose     plain-language description of what consent covers
 * @param state       whether the link is still actionable, spent, or expired
 */
record ConsentContext(String menteeEmail, String guardianName, String purpose, ConsentLinkState state) {
}
