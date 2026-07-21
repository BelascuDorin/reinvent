package com.reinvent.platform.testing;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.reinvent.platform.GuardianNotifier;

/**
 * In-memory {@link GuardianNotifier} fake: records the consent links it would
 * have sent so tests can assert on them.
 */
public class InMemoryGuardianNotifier implements GuardianNotifier {

	public record SentConsentLink(String guardianEmail, URI consentLink) {
	}

	private final List<SentConsentLink> sent = new ArrayList<>();

	@Override
	public void sendConsentLink(String guardianEmail, URI consentLink) {
		sent.add(new SentConsentLink(guardianEmail, consentLink));
	}

	public List<SentConsentLink> sent() {
		return List.copyOf(sent);
	}
}
