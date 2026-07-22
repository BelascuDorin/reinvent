/**
 * Shared kernel: the ports through which the domain reaches the outside world
 * ({@link com.reinvent.platform.PaymentGateway}, {@link com.reinvent.platform.VideoProvider},
 * {@link com.reinvent.platform.GuardianNotifier}) and time
 * ({@link com.reinvent.platform.Clock}), plus cross-cutting endpoints such as status.
 *
 * <p>It also holds the cross-module domain events that two bounded contexts meet on
 * ({@link com.reinvent.platform.MinorMenteeRegistered},
 * {@link com.reinvent.platform.GuardianConsentGranted}): keeping the event types here,
 * where every module already depends, lets identity and consent communicate without
 * either depending on the other — and so without a module cycle.
 *
 * <p>Declared {@code OPEN} so every domain module may depend on it without an
 * explicit allow-list — it is the shared kernel, not a bounded context.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.reinvent.platform;
