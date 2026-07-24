package com.reinvent.mentorship.internal;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The read-side of the curated Field vocabulary: lists the seeded Fields in their
 * fixed browse order. A separate concern from the Mentor application lifecycle
 * ({@link MentorshipService}), so it lives on its own service. The vocabulary is
 * seeded by Flyway and never written from the application, hence read-only.
 */
@Service
@Transactional(readOnly = true)
class FieldCatalog {

	private final FieldRepository fields;

	FieldCatalog(FieldRepository fields) {
		this.fields = fields;
	}

	/** The curated Fields, in their fixed browse order. */
	List<FieldView> list() {
		return fields.findAllByOrderBySortOrderAsc().stream().map(FieldView::of).toList();
	}
}
