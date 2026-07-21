package com.reinvent.platform.testing;

import java.net.URI;
import java.util.UUID;

import com.reinvent.platform.VideoProvider;

/**
 * In-memory {@link VideoProvider} fake: returns a deterministic, inspectable
 * Meeting link instead of calling a real provider.
 */
public class InMemoryVideoProvider implements VideoProvider {

	@Override
	public URI createMeetingLink(UUID meetingId) {
		return URI.create("https://video.test.reinvent/" + meetingId);
	}
}
