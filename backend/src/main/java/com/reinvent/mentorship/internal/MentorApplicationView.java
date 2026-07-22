package com.reinvent.mentorship.internal;

import java.util.UUID;

/**
 * A view of a Mentor application, for both the applicant (their own status) and a
 * Reviewer (queue entries). {@code bookable} reflects the full predicate (approved,
 * not suspended, and payment-onboarded), so a freshly approved Mentor sees
 * {@code approved=true} but {@code bookable=false} until they onboard (ADR-0003).
 */
record MentorApplicationView(
		UUID id,
		UUID applicantUserId,
		ApplicationStatus status,
		String rejectionReason,
		boolean suspended,
		boolean bookable) {

	static MentorApplicationView of(MentorApplication application, boolean bookable) {
		return new MentorApplicationView(application.id(), application.applicantUserId(), application.status(),
				application.rejectionReason(), application.isSuspended(), bookable);
	}
}
