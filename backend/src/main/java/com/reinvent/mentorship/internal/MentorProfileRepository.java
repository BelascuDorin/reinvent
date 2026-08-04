package com.reinvent.mentorship.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {

	/**
	 * Profiles matching a discovery search's Field and language, either of which may be
	 * omitted (null = don't filter on it). This narrows on the search criteria only —
	 * plain data — and deliberately not on whether the Mentor is discoverable: that rule
	 * lives on {@link MentorProfile#isComplete()} and {@link MentorApplication}, and
	 * restating it in a query would give it a second home to drift from.
	 */
	@Query("""
			SELECT p FROM MentorProfile p
			WHERE (:fieldSlug IS NULL OR :fieldSlug MEMBER OF p.fieldSlugs)
			  AND (:language IS NULL OR :language MEMBER OF p.languages)
			""")
	List<MentorProfile> findMatching(@Param("fieldSlug") String fieldSlug, @Param("language") String language);
}
