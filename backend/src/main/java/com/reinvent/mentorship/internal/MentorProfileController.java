package com.reinvent.mentorship.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.reinvent.identity.CurrentUser;
import com.reinvent.identity.Role;

/**
 * The Mentor's own boundary onto their profile. Gated to the MENTOR role via
 * {@link CurrentUser} — a Mentee or visitor is refused (403) — and scoped to the
 * caller's own id, so a Mentor can only ever read their own profile, never another's.
 */
@RestController
@RequestMapping("/api/mentor-profiles")
class MentorProfileController {

	private final MentorProfileService mentorProfiles;
	private final CurrentUser currentUser;

	MentorProfileController(MentorProfileService mentorProfiles, CurrentUser currentUser) {
		this.mentorProfiles = mentorProfiles;
		this.currentUser = currentUser;
	}

	@GetMapping("/me")
	MentorProfileView mine() {
		currentUser.requireRole(Role.MENTOR);
		return mentorProfiles.myProfile(currentUser.requireUserId());
	}

	@PutMapping("/me")
	MentorProfileView update(@RequestBody @Valid UpdateMentorProfileRequest edit) {
		currentUser.requireRole(Role.MENTOR);
		return mentorProfiles.updateMyProfile(currentUser.requireUserId(), edit);
	}
}
