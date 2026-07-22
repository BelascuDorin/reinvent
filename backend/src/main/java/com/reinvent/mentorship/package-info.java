/**
 * The {@code mentorship} module: the Mentor application lifecycle
 * ({@code applied → under review → approved | rejected}), Reviewer actions,
 * Mentor suspension, and the bookability predicate (ADR-0001, ADR-0003).
 *
 * <p>It reads the current actor and enforces the REVIEWER gate through identity's
 * {@link com.reinvent.identity.CurrentUser} API, and on approval publishes
 * {@link com.reinvent.platform.MentorApproved} so identity grants the MENTOR role —
 * mentorship never touches the role model itself. Bookability is exposed as
 * {@link com.reinvent.mentorship.MentorBookability} for the later booking slice to
 * consume; nothing calls it yet.
 */
@org.springframework.modulith.ApplicationModule
package com.reinvent.mentorship;
