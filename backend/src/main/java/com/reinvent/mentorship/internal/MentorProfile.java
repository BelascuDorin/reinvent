package com.reinvent.mentorship.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * A Mentor's public presentation (spec 0002), one per Mentor, keyed by their User id.
 * Provisioned as an empty draft when the Reviewer approves the Mentor, then filled in
 * by the owner. The aggregate owns its own completeness rule: a profile is
 * <em>complete</em> — the profile-local half of discoverability — once it has at
 * least one Field, a price, and a Meeting duration. Whether the Mentor is actually
 * discoverable additionally depends on the application being approved and not
 * suspended; {@link MentorDiscoverability} composes the two halves (that state lives
 * on {@link MentorApplication}).
 */
@Entity
@Table(name = "mentor_profile")
class MentorProfile {

	@Id
	@Column(name = "mentor_user_id")
	private UUID mentorUserId;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "role_title")
	private String roleTitle;

	private String bio;

	private String experience;

	private String employer;

	@Column(name = "price_amount")
	private BigDecimal priceAmount;

	@Column(name = "price_currency")
	private String priceCurrency;

	@Column(name = "meeting_duration_minutes")
	private Integer meetingDurationMinutes;

	@ElementCollection
	@CollectionTable(name = "mentor_profile_field", joinColumns = @JoinColumn(name = "mentor_user_id"))
	@Column(name = "field_slug")
	private Set<String> fieldSlugs = new HashSet<>();

	@ElementCollection
	@CollectionTable(name = "mentor_profile_language", joinColumns = @JoinColumn(name = "mentor_user_id"))
	@Column(name = "language")
	private Set<String> languages = new HashSet<>();

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected MentorProfile() {
		// for JPA
	}

	/**
	 * A blank draft for a freshly approved Mentor. Per the discovery slice's decision,
	 * nothing is seeded from the application (it carries no presentation fields), so the
	 * Mentor starts from an empty — and therefore not-yet-complete — profile.
	 */
	static MentorProfile draft(UUID mentorUserId, Instant now) {
		MentorProfile profile = new MentorProfile();
		profile.mentorUserId = mentorUserId;
		profile.createdAt = now;
		profile.updatedAt = now;
		return profile;
	}

	/**
	 * Replace the owner-editable presentation with the values from an edit. A full
	 * replacement (not a patch): the Field and language sets become exactly what was
	 * sent, so clearing them here is how a Mentor edits themselves back out of
	 * discovery. {@code employer} is optional — a blank value is normalised to null.
	 * Field slugs are validated against the curated set by the caller before this runs.
	 */
	void update(UpdateMentorProfileRequest edit, Instant now) {
		this.displayName = blankToNull(edit.displayName());
		this.roleTitle = blankToNull(edit.roleTitle());
		this.bio = blankToNull(edit.bio());
		this.experience = blankToNull(edit.experience());
		this.employer = blankToNull(edit.employer());
		this.priceAmount = edit.priceAmount();
		this.priceCurrency = blankToNull(edit.priceCurrency());
		this.meetingDurationMinutes = edit.meetingDurationMinutes();
		this.fieldSlugs = new HashSet<>(edit.fieldSlugs() == null ? Set.of() : edit.fieldSlugs());
		this.languages = new HashSet<>(edit.languages() == null ? Set.of() : edit.languages());
		this.updatedAt = now;
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

	/** The profile-local half of discoverability: enough filled in to be worth showing. */
	boolean isComplete() {
		return !fieldSlugs.isEmpty() && hasPrice() && meetingDurationMinutes != null;
	}

	private boolean hasPrice() {
		return priceAmount != null && priceCurrency != null && !priceCurrency.isBlank();
	}

	UUID mentorUserId() {
		return mentorUserId;
	}

	String displayName() {
		return displayName;
	}

	String roleTitle() {
		return roleTitle;
	}

	String bio() {
		return bio;
	}

	String experience() {
		return experience;
	}

	String employer() {
		return employer;
	}

	BigDecimal priceAmount() {
		return priceAmount;
	}

	String priceCurrency() {
		return priceCurrency;
	}

	Integer meetingDurationMinutes() {
		return meetingDurationMinutes;
	}

	Set<String> fieldSlugs() {
		return fieldSlugs;
	}

	Set<String> languages() {
		return languages;
	}
}
