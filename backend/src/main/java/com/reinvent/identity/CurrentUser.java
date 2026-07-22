package com.reinvent.identity;

import java.util.UUID;

/**
 * The signed-in User behind the current request, and the module's single point of
 * authorization. Other modules depend on this rather than reading the session or
 * the role model themselves — identity owns both.
 *
 * <p>This is the lightweight guard the accounts slice settled on (HttpSession +
 * roles), not Spring Security; it is enough for the one REVIEWER-gated surface so
 * far and can be swapped for a filter-chain later without changing callers.
 */
public interface CurrentUser {

	/**
	 * @return the signed-in User's id
	 * @throws RuntimeException mapped to 401 if there is no authenticated session
	 */
	UUID requireUserId();

	/**
	 * Asserts the signed-in User holds {@code role}.
	 *
	 * @throws RuntimeException mapped to 401 if not signed in, or 403 if signed in
	 *                          without the role
	 */
	void requireRole(Role role);
}
