package com.reinvent.mentorship.internal;

/**
 * The Mentor application lifecycle (ADR-0001): {@code APPLIED → UNDER_REVIEW →
 * APPROVED | REJECTED}. Only these transitions are legal; the aggregate refuses
 * any other. Suspension is deliberately not a status here — a suspended Mentor's
 * application stays {@code APPROVED}; suspension is tracked separately.
 */
enum ApplicationStatus {
	APPLIED,
	UNDER_REVIEW,
	APPROVED,
	REJECTED;

	/** Whether an application in this status blocks the User from opening another. */
	boolean blocksReapplication() {
		// Only a rejected applicant may apply afresh; an open or approved one may not.
		return this != REJECTED;
	}
}
