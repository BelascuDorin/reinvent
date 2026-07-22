package com.reinvent.platform;

import java.util.UUID;

/**
 * A Reviewer has approved a User's Mentor application (ADR-0001). Published by the
 * {@code mentorship} module and consumed by {@code identity}, which grants the
 * MENTOR role — role membership is identity's to own, so mentorship never touches
 * it directly. Lives in the shared kernel so the two modules meet only on this
 * contract and neither depends on the other.
 *
 * @param userId the approved applicant's User id
 */
public record MentorApproved(UUID userId) {
}
