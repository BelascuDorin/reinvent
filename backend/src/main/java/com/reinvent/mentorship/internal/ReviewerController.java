package com.reinvent.mentorship.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reinvent.identity.CurrentUser;
import com.reinvent.identity.Role;

import jakarta.validation.Valid;

/**
 * The Reviewer's boundary: work the application queue and act on applications. Every
 * action is gated to the REVIEWER role via {@link CurrentUser}, so a Mentee or Mentor
 * hitting these endpoints is refused (403).
 */
@RestController
@RequestMapping("/api/reviewer/applications")
class ReviewerController {

	private final MentorshipService mentorship;
	private final CurrentUser currentUser;

	ReviewerController(MentorshipService mentorship, CurrentUser currentUser) {
		this.mentorship = mentorship;
		this.currentUser = currentUser;
	}

	@GetMapping
	List<MentorApplicationView> queue() {
		currentUser.requireRole(Role.REVIEWER);
		return mentorship.queue();
	}

	@GetMapping("/mentors")
	List<MentorApplicationView> approvedMentors() {
		currentUser.requireRole(Role.REVIEWER);
		return mentorship.approvedMentors();
	}

	@PostMapping("/{id}/start-review")
	MentorApplicationView startReview(@PathVariable UUID id) {
		currentUser.requireRole(Role.REVIEWER);
		return mentorship.startReview(id);
	}

	@PostMapping("/{id}/approve")
	MentorApplicationView approve(@PathVariable UUID id) {
		currentUser.requireRole(Role.REVIEWER);
		return mentorship.approve(id);
	}

	@PostMapping("/{id}/reject")
	MentorApplicationView reject(@PathVariable UUID id, @RequestBody @Valid RejectionRequest request) {
		currentUser.requireRole(Role.REVIEWER);
		return mentorship.reject(id, request.reason());
	}

	@PostMapping("/{id}/suspend")
	MentorApplicationView suspend(@PathVariable UUID id) {
		currentUser.requireRole(Role.REVIEWER);
		return mentorship.suspend(id);
	}
}
