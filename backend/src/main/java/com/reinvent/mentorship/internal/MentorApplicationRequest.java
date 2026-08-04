package com.reinvent.mentorship.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * What an applicant writes when they apply for the Mentor role. Both parts are required:
 * a Reviewer approves applications by hand (ADR-0001) and cannot vet an empty one, and
 * these are what seed the Mentor's draft profile on approval, so an approved Mentor has
 * something to start from.
 *
 * <p>The headline becomes the profile's role/title. Everything else about how a Mentor
 * presents themselves — price, languages, employer, Meeting duration, Field(s) — belongs
 * to the profile editor, not to applying.
 */
record MentorApplicationRequest(
		@NotBlank @Size(max = 200) String headline,
		@NotBlank @Size(max = 5000) String bio) {
}
