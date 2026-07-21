package com.reinvent.platform;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Placeholder {@link GuardianNotifier} for the walking skeleton: it logs the
 * consent link instead of emailing it. Replaced by a real email/notification
 * adapter in the consent slice.
 */
@Component
class LoggingGuardianNotifier implements GuardianNotifier {

	private static final Logger log = LoggerFactory.getLogger(LoggingGuardianNotifier.class);

	@Override
	public void sendConsentLink(String guardianEmail, URI consentLink) {
		log.info("Would send Guardian consent link to {}: {}", guardianEmail, consentLink);
	}
}
