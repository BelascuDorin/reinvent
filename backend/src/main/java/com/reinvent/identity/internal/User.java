package com.reinvent.identity.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.reinvent.identity.GuardianConsentStatus;
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

	/**
	 * This module's projection of where the User stands on Guardian consent. An
	 * adult is {@link GuardianConsentStatus#NOT_REQUIRED}; a minor starts
	 * {@link GuardianConsentStatus#PENDING_GUARDIAN_CONSENT} and is flipped to
	 * {@link GuardianConsentStatus#CONSENTED} when the consent module reports the
	 * Guardian consented. The consent module owns the consent record; this is only
	 * the account-state view identity surfaces.
	 */
	@Column(name = "guardian_consent_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private GuardianConsentStatus guardianConsentStatus;

	protected User() {
		// for JPA
	}

	User(UUID id, String email, String passwordHash, LocalDate dateOfBirth, Instant createdAt,
			GuardianConsentStatus guardianConsentStatus) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.dateOfBirth = dateOfBirth;
		this.createdAt = createdAt;
		this.guardianConsentStatus = guardianConsentStatus;
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

	GuardianConsentStatus guardianConsentStatus() {
		return guardianConsentStatus;
	}

	/**
	 * Records that the Guardian has consented, lifting the pending gate. A no-op
	 * for a User who is not (or no longer) awaiting consent, so a late or duplicate
	 * grant event can never regress {@code NOT_REQUIRED} or re-open a settled state.
	 */
	void markGuardianConsented() {
		if (guardianConsentStatus == GuardianConsentStatus.PENDING_GUARDIAN_CONSENT) {
			guardianConsentStatus = GuardianConsentStatus.CONSENTED;
		}
	}

	/**
	 * Grants the MENTOR role (additive — the User keeps MENTEE). Idempotent, since
	 * {@code roles} is a set, so a replayed approval event is harmless.
	 */
	void grantMentorRole() {
		roles.add(Role.MENTOR);
	}

	/** Whether this User is under 18 at the given instant (evaluated in UTC). */
	boolean isMinorAt(Instant now) {
		return isMinorAt(dateOfBirth, now);
	}

	/** Whether someone born on {@code dateOfBirth} is under 18 at {@code now} (UTC). */
	static boolean isMinorAt(LocalDate dateOfBirth, Instant now) {
		LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
		return dateOfBirth.plusYears(18).isAfter(today);
	}
}
