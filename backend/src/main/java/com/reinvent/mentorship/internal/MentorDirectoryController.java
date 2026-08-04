package com.reinvent.mentorship.internal;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The public discovery boundary: browse and search Mentors by Field and language.
 * Unauthenticated by design — like {@link FieldController} it calls no
 * {@link com.reinvent.identity.CurrentUser} guard — so a Mentee can look for a Mentor
 * before signing up, and the page stays server-renderable and indexable (spec 0002).
 */
@RestController
@RequestMapping("/api/mentors")
class MentorDirectoryController {

	private final MentorDirectory directory;

	MentorDirectoryController(MentorDirectory directory) {
		this.directory = directory;
	}

	@GetMapping
	List<MentorSummaryView> search(@RequestParam(required = false) String field,
			@RequestParam(required = false) String language) {
		return directory.search(field, language);
	}
}
