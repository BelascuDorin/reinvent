package com.reinvent.platform;

import java.time.Instant;
import java.util.UUID;

/**
 * A Guardian has given one-time, platform-level consent for a minor Mentee
 * (ADR-0002). The consent record is the source of truth; this event lets the
 * {@code identity} module lift the {@code PENDING_GUARDIAN_CONSENT} gate on the
 * minor's account.
 *
 * <p>Published by the {@code consent} module and consumed by {@code identity}.
 * Lives in the shared kernel so the two modules meet only on this contract.
 *
 * @param userId      the minor Mentee's User id whose gate is lifted
 * @param consentedAt when the Guardian consented (per the {@link Clock} port)
 */
public record GuardianConsentGranted(UUID userId, Instant consentedAt) {
}
