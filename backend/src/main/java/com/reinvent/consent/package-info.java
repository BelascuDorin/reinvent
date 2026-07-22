/**
 * The {@code consent} module: the Guardian, one-time platform-level Guardian
 * consent, and the single-use consent-link flow (ADR-0002).
 *
 * <p>On {@link com.reinvent.platform.MinorMenteeRegistered} it raises a Guardian
 * consent request and sends the link via the {@link com.reinvent.platform.GuardianNotifier}
 * port. When the Guardian consents it records the consent (who/when/which minor —
 * not identity proof) and publishes {@link com.reinvent.platform.GuardianConsentGranted}
 * so identity can lift the minor's gate. The two modules meet only on those shared-kernel
 * events, never on each other's internals.
 */
@org.springframework.modulith.ApplicationModule
package com.reinvent.consent;
