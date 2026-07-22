package com.reinvent.consent.internal;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Consent-flow configuration.
 *
 * @param linkBaseUrl the front-end origin the Guardian consent link points at; the
 *                    token is appended as {@code /guardian-consent/{token}}
 * @param linkTtl     how long a freshly issued consent link stays valid
 */
@ConfigurationProperties("reinvent.consent")
record ConsentProperties(
		@DefaultValue("http://localhost:3000") String linkBaseUrl,
		@DefaultValue("7d") Duration linkTtl) {
}
