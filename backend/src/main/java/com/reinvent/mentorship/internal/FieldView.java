package com.reinvent.mentorship.internal;

/**
 * The public view of a curated {@link Field}: the stable {@code slug} a caller
 * filters or selects by, and the {@code displayName} a visitor reads. The ordering
 * is carried by the list, not the item, so {@code sortOrder} is not exposed.
 */
record FieldView(String slug, String displayName) {

	static FieldView of(Field field) {
		return new FieldView(field.slug(), field.displayName());
	}
}
