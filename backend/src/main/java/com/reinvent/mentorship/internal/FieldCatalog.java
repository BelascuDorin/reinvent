package com.reinvent.mentorship.internal;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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

	/**
	 * Whether every one of these slugs names a curated Field — the check anything
	 * referencing the vocabulary goes through, so the curated set has a single owner.
	 * An empty request trivially holds; a null slug never names a Field.
	 */
	boolean containsAll(Collection<String> slugs) {
		if (slugs == null || slugs.isEmpty()) {
			return true;
		}
		if (slugs.contains(null)) {
			return false;
		}
		Set<String> requested = Set.copyOf(slugs);
		return fields.findAllById(requested).size() == requested.size();
	}
}
