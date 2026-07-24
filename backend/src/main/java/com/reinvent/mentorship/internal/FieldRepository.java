package com.reinvent.mentorship.internal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface FieldRepository extends JpaRepository<Field, String> {

	/** The curated Fields in their fixed browse order. */
	List<Field> findAllByOrderBySortOrderAsc();
}
