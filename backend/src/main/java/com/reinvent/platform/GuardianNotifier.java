package com.reinvent.platform;

import java.net.URI;

/**
 * Port for reaching a Guardian, who is not a User of the product.
 *
 * <p>At launch its only job is delivering the one-time Guardian consent link
 * (ADR-0002); booking notifications to the Guardian come later.
 */
public interface GuardianNotifier {

	void sendConsentLink(String guardianEmail, URI consentLink);
}
