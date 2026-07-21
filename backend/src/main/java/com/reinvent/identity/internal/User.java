package com.reinvent.identity.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.reinvent.identity.Role;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * A single person's account. The {@code id} is the stable identity used later to
 * enforce that a User can never be both parties of a Meeting: booking will
 * compare this id on both sides and reject a self-booking.
 */
@Entity
@Table(name = "app_user")
class User {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "date_of_birth", nullable = false)
	private LocalDate dateOfBirth;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private Set<Role> roles = new HashSet<>();

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected User() {
		// for JPA
	}

	User(UUID id, String email, String passwordHash, LocalDate dateOfBirth, Instant createdAt) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.dateOfBirth = dateOfBirth;
		this.createdAt = createdAt;
		this.roles.add(Role.MENTEE); // everyone is a Mentee by default
	}

	UUID id() {
		return id;
	}

	String email() {
		return email;
	}

	String passwordHash() {
		return passwordHash;
	}

	Set<Role> roles() {
		return Set.copyOf(roles);
	}

	/** Whether this User is under 18 at the given instant (evaluated in UTC). */
	boolean isMinorAt(Instant now) {
		LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
		return dateOfBirth.plusYears(18).isAfter(today);
	}
}
