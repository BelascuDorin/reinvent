package com.reinvent.consent.internal;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reinvent.platform.Clock;
import com.reinvent.platform.GuardianConsentGranted;
import com.reinvent.platform.GuardianNotifier;
import com.reinvent.platform.MinorMenteeRegistered;

/**
 * The Guardian-consent flow: raise a single-use link when a minor registers, let a
 * Guardian resolve the link's context, and record consent once — refusing a spent
 * or expired link (ADR-0002). Recording consent publishes {@link GuardianConsentGranted}
 * so the identity module can lift the minor's gate.
 */
@Service
@Transactional
class ConsentService {

	static final String PURPOSE = "Consent for your teen to use Reinvent to discover and meet vetted Mentors.";

	private final GuardianConsentRepository consents;
	private final GuardianNotifier guardianNotifier;
	private final Clock clock;
	private final ConsentProperties properties;
	private final ApplicationEventPublisher events;

	ConsentService(GuardianConsentRepository consents, GuardianNotifier guardianNotifier, Clock clock,
			ConsentProperties properties, ApplicationEventPublisher events) {
		this.consents = consents;
		this.guardianNotifier = guardianNotifier;
		this.clock = clock;
		this.properties = properties;
		this.events = events;
	}

	/**
	 * On a minor Mentee registering, raise a Guardian consent request and send its
	 * single-use link. Runs after the identity signup transaction commits.
	 */
	@ApplicationModuleListener
	void on(MinorMenteeRegistered event) {
		Instant now = clock.now();
		UUID token = UUID.randomUUID();
		consents.save(new GuardianConsent(token, event.userId(), event.menteeEmail(), event.guardianName(),
				event.guardianEmail(), now, now.plus(properties.linkTtl())));
		guardianNotifier.sendConsentLink(event.guardianEmail(), linkFor(token));
	}

	/** Resolves a link to the context the Guardian needs to decide (throws if unknown). */
	@Transactional(readOnly = true)
	ConsentContext resolve(UUID token) {
		GuardianConsent consent = consents.findById(token).orElseThrow(ConsentLinkNotFoundException::new);
		return new ConsentContext(consent.menteeEmail(), consent.guardianName(), PURPOSE, stateOf(consent));
	}

	/**
	 * Records the Guardian's consent for the link's minor, exactly once. Refuses an
	 * unknown, already-used, or expired link, and refuses a submission that does not
	 * affirm both guardianship and agreement.
	 *
	 * <p>A spent link is refused explicitly (issue #4: "reused ... links fail
	 * explicitly"), deliberately over spec 0001's softer "idempotent for the same
	 * link" wording: a trustworthy consent record should not silently accept a
	 * replay. The front end guards against accidental double-submits so a Guardian
	 * does not hit this on a double-click.
	 */
	void submit(UUID token, ConsentSubmission submission) {
		GuardianConsent consent = consents.findById(token).orElseThrow(ConsentLinkNotFoundException::new);
		if (consent.isConsented()) {
			throw new ConsentLinkAlreadyUsedException();
		}
		Instant now = clock.now();
		if (consent.isExpiredAt(now)) {
			throw new ConsentLinkExpiredException();
		}
		if (!submission.isAffirmed()) {
			throw new ConsentNotAffirmedException();
		}
		consent.consent(now);
		events.publishEvent(new GuardianConsentGranted(consent.userId(), now));
	}

	private ConsentLinkState stateOf(GuardianConsent consent) {
		if (consent.isConsented()) {
			return ConsentLinkState.CONSENTED;
		}
		return consent.isExpiredAt(clock.now()) ? ConsentLinkState.EXPIRED : ConsentLinkState.PENDING;
	}

	private URI linkFor(UUID token) {
		String base = properties.linkBaseUrl().replaceAll("/+$", "");
		return URI.create(base + "/guardian-consent/" + token);
	}
}
