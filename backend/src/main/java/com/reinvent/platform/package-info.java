/**
 * Shared kernel: the ports through which the domain reaches the outside world
 * ({@link com.reinvent.platform.PaymentGateway}, {@link com.reinvent.platform.VideoProvider},
 * {@link com.reinvent.platform.GuardianNotifier}) and time
 * ({@link com.reinvent.platform.Clock}), plus cross-cutting endpoints such as status.
 *
 * <p>Declared {@code OPEN} so every domain module may depend on it without an
 * explicit allow-list — it is the shared kernel, not a bounded context.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.reinvent.platform;
