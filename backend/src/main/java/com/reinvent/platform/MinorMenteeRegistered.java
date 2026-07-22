package com.reinvent.platform;

import java.util.UUID;

/**
 * A minor Mentee has signed up and named a Guardian who must give platform-level
 * consent before the minor can perform gated actions (ADR-0002).
 *
 * <p>Published by the {@code identity} module at signup and consumed by the
 * {@code consent} module, which raises the Guardian consent request and sends the
 * link. It lives in the shared kernel so neither bounded context depends on the
 * other's internals — they meet only on this contract.
 *
 * @param userId        the minor Mentee's User id
 * @param menteeEmail   the minor's email, so the Guardian can see which minor the
 *                      link is about (context, not identity proof)
 * @param guardianName  the Guardian's name as stated by the minor at signup
 * @param guardianEmail where the consent link is sent (the Guardian is not a User)
 */
public record MinorMenteeRegistered(UUID userId, String menteeEmail, String guardianName, String guardianEmail) {
}
