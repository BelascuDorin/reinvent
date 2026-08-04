package com.reinvent.mentorship.internal;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A User's application to take on the Mentor role and its lifecycle (ADR-0001).
 * The aggregate owns the state machine: only legal transitions are permitted and
 * any other is refused, so the lifecycle can't be corrupted. Suspension is a
 * separate flag on an approved application, not a lifecycle state.
 */
@Entity
@Table(name = "mentor_application")
class MentorApplication {

	@Id
	private UUID id;

	@Column(name = "applicant_user_id", nullable = false)
	private UUID applicantUserId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ApplicationStatus status;

	@Column(name = "rejection_reason")
	private String rejectionReason;

	@Column(nullable = false)
	private boolean suspended;

	/** Which attempt this is for the applicant: their first application is 1. */
	@Column(nullable = false)
	private int attempt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected MentorApplication() {
		// for JPA
	}

	MentorApplication(UUID id, UUID applicantUserId, int attempt, Instant now) {
		this.id = id;
		this.applicantUserId = applicantUserId;
		this.attempt = attempt;
		this.status = ApplicationStatus.APPLIED;
		this.suspended = false;
		this.createdAt = now;
		this.updatedAt = now;
	}

	/**
	 * The one that counts out of a User's application history — the single definition of
	 * "where this User stands", so no two callers can disagree.
	 *
	 * <p>A User may hold at most one application that is not rejected (the database
	 * enforces it), and that one is always the current one: it is either still being
	 * decided or it is the approval they are living under. Only once every attempt has
	 * been rejected does recency matter, and then it is the latest attempt — never the
	 * timestamp, which several applications can share.
	 */
	static Optional<MentorApplication> current(Collection<MentorApplication> history) {
		return history.stream()
				.filter(application -> application.status != ApplicationStatus.REJECTED)
				.findFirst()
				.or(() -> history.stream().max(Comparator.comparingInt(MentorApplication::attempt)));
	}

	int attempt() {
		return attempt;
	}

	UUID id() {
		return id;
	}

	UUID applicantUserId() {
		return applicantUserId;
	}

	ApplicationStatus status() {
		return status;
	}

	String rejectionReason() {
		return rejectionReason;
	}

	boolean isSuspended() {
		return suspended;
	}

	/**
	 * An approved Mentor still in circulation — the shared half of both bookability
	 * (ADR-0003) and discovery visibility (spec 0002). Owned here so the rule lives on
	 * the aggregate, not re-derived by each service that needs it.
	 */
	boolean isActiveMentor() {
		return status == ApplicationStatus.APPROVED && !suspended;
	}

	/** A Reviewer picks the application up for vetting. */
	void startReview(Instant now) {
		requireStatus(ApplicationStatus.APPLIED, "start review of");
		transitionTo(ApplicationStatus.UNDER_REVIEW, now);
	}

	/** A Reviewer approves the application; the caller grants the MENTOR role. */
	void approve(Instant now) {
		requireStatus(ApplicationStatus.UNDER_REVIEW, "approve");
		transitionTo(ApplicationStatus.APPROVED, now);
	}

	/** A Reviewer rejects the application with a reason. */
	void reject(String reason, Instant now) {
		requireStatus(ApplicationStatus.UNDER_REVIEW, "reject");
		this.rejectionReason = reason;
		transitionTo(ApplicationStatus.REJECTED, now);
	}

	/** A Reviewer suspends an approved Mentor, taking them out of circulation. */
	void suspend(Instant now) {
		requireStatus(ApplicationStatus.APPROVED, "suspend the Mentor of");
		if (suspended) {
			throw new IllegalApplicationTransitionException("This Mentor is already suspended.");
		}
		this.suspended = true;
		this.updatedAt = now;
	}

	/**
	 * A Reviewer lifts the suspension, putting the Mentor back in circulation. Nothing
	 * about their presentation was destroyed while they were hidden, so there is nothing
	 * to restore beyond the flag — they are discoverable again the moment this returns
	 * (given a complete profile).
	 */
	void reinstate(Instant now) {
		requireStatus(ApplicationStatus.APPROVED, "reinstate the Mentor of");
		if (!suspended) {
			throw new IllegalApplicationTransitionException("This Mentor is not suspended.");
		}
		this.suspended = false;
		this.updatedAt = now;
	}

	private void requireStatus(ApplicationStatus expected, String action) {
		if (status != expected) {
			throw new IllegalApplicationTransitionException(
					"Cannot " + action + " an application that is " + status + ".");
		}
	}

	private void transitionTo(ApplicationStatus next, Instant now) {
		this.status = next;
		this.updatedAt = now;
	}
}
