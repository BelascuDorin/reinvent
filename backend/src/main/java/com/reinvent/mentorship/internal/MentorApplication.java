package com.reinvent.mentorship.internal;

import java.time.Instant;
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

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected MentorApplication() {
		// for JPA
	}

	MentorApplication(UUID id, UUID applicantUserId, Instant now) {
		this.id = id;
		this.applicantUserId = applicantUserId;
		this.status = ApplicationStatus.APPLIED;
		this.suspended = false;
		this.createdAt = now;
		this.updatedAt = now;
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
