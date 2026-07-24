package com.reinvent.mentorship.internal;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The public catalogue boundary: list the curated Fields. Unauthenticated by
 * design — it calls no {@link com.reinvent.identity.CurrentUser} guard — so any
 * visitor can browse the career areas before signing up (spec 0002).
 */
@RestController
@RequestMapping("/api/fields")
class FieldController {

	private final FieldCatalog catalog;

	FieldController(FieldCatalog catalog) {
		this.catalog = catalog;
	}

	@GetMapping
	List<FieldView> list() {
		return catalog.list();
	}
}
