package com.reinvent.identity.internal;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.reinvent.platform.MentorApproved;

/**
 * Grants the MENTOR role when the mentorship module reports a Reviewer approved a
 * Mentor application (ADR-0001). Role membership is identity's to own; mentorship
 * decides the outcome and announces it, identity applies it. Runs transactionally
 * after the approving transaction commits.
 */
@Component
class MentorApprovedListener {

	private final UserRepository users;

	MentorApprovedListener(UserRepository users) {
		this.users = users;
	}

	@ApplicationModuleListener
	void on(MentorApproved event) {
		users.findById(event.userId()).ifPresent(User::grantMentorRole);
	}
}
