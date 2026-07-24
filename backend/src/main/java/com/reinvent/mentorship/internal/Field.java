package com.reinvent.mentorship.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A curated career area a Mentor can practise and a Mentee can browse by (spec
 * 0002). Fields are a controlled vocabulary owned by the Flyway seed, not a Java
 * enum, so the list is business-configurable without a redeploy — this aggregate is
 * therefore read-only from the application's point of view (no constructor for new
 * rows, no mutators). The {@code slug} is the stable key a Mentor profile references.
 */
@Entity
@Table(name = "field")
class Field {

	@Id
	private String slug;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	protected Field() {
		// for JPA
	}

	String slug() {
		return slug;
	}

	String displayName() {
		return displayName;
	}
}
