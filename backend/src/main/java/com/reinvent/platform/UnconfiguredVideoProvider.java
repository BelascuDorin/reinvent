package com.reinvent.platform;

import java.net.URI;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Placeholder {@link VideoProvider} for the walking skeleton. Nothing books a
 * Meeting yet, so any attempt to create a Meeting link is a wiring mistake and
 * fails loudly rather than handing back a fake link. Replaced by the Jitsi
 * adapter in the booking slice.
 */
@Component
class UnconfiguredVideoProvider implements VideoProvider {

	@Override
	public URI createMeetingLink(UUID meetingId) {
		throw new UnsupportedOperationException(
				"No VideoProvider is configured yet; Meeting links arrive with the booking slice");
	}
}
