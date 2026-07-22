package com.reinvent.consent.internal;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One Guardian consent request for one minor Mentee: the single-use, single-purpose
 * link and, once acted on, the consent record itself (who consented, when, for which
 * minor). It is explicitly not proof of the Guardian's or the minor's identity
 * (ADR-0002).
 *
 * <p>The {@code id} doubles as the opaque link token. A request is single-purpose
 * because the token maps to exactly one minor, and single-use because consenting
 * flips it to {@link ConsentStatus#CONSENTED} and a spent link is refused.
 */
@Entity
@Table(name = "guardian_consent")
class GuardianConsent {

	@Id
	private UUID token;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "mentee_email", nullable = false)
	private String menteeEmail;

	@Column(name = "guardian_name", nullable = false)
	private String guardianName;

	@Column(name = "guardian_email", nullable = false)
	private String guardianEmail;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ConsentStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "consented_at")
	private Instant consentedAt;

	protected GuardianConsent() {
		// for JPA
	}

	GuardianConsent(UUID token, UUID userId, String menteeEmail, String guardianName, String guardianEmail,
			Instant createdAt, Instant expiresAt) {
		this.token = token;
		this.userId = userId;
		this.menteeEmail = menteeEmail;
		this.guardianName = guardianName;
		this.guardianEmail = guardianEmail;
		this.status = ConsentStatus.PENDING;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}

	UUID token() {
		return token;
	}

	UUID userId() {
		return userId;
	}

	String menteeEmail() {
		return menteeEmail;
	}

	String guardianName() {
		return guardianName;
	}

	boolean isConsented() {
		return status == ConsentStatus.CONSENTED;
	}

	boolean isExpiredAt(Instant now) {
		return now.isAfter(expiresAt);
	}

	Instant consentedAt() {
		return consentedAt;
	}

	/**
	 * Records the Guardian's consent at {@code now}. The caller is responsible for
	 * having already refused a spent or expired link.
	 */
	void consent(Instant now) {
		this.status = ConsentStatus.CONSENTED;
		this.consentedAt = now;
	}
}
