package com.reinvent.mentorship.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reinvent.identity.CurrentUser;

/** The applicant's boundary: apply to be a Mentor and check your own status. */
@RestController
@RequestMapping("/api/mentor-applications")
class MentorApplicationController {

	private final MentorshipService mentorship;
	private final CurrentUser currentUser;

	MentorApplicationController(MentorshipService mentorship, CurrentUser currentUser) {
		this.mentorship = mentorship;
		this.currentUser = currentUser;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	MentorApplicationView apply() {
		return mentorship.apply(currentUser.requireUserId());
	}

	@GetMapping("/me")
	MentorApplicationView mine() {
		return mentorship.myApplication(currentUser.requireUserId());
	}
}
