package com.reinvent.platform;

import java.net.URI;
import java.util.UUID;

/**
 * Port to the video venue for a Meeting (Jitsi at launch, per ADR-0004).
 *
 * <p>The domain never depends on a specific provider — only on this port, which
 * yields a Meeting link. Not exercised in the walking skeleton; it exists so the
 * booking slice plugs a real provider in without touching the domain.
 */
public interface VideoProvider {

	URI createMeetingLink(UUID meetingId);
}
