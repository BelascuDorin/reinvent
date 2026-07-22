package com.reinvent.identity.internal;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.reinvent.identity.CurrentUser;
import com.reinvent.identity.Role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Resolves the signed-in User from the HTTP session and enforces role checks. The
 * {@link HttpServletRequest} is a Spring scoped-proxy, so this singleton reads the
 * session of whichever request is in flight.
 */
@Component
class CurrentUserImpl implements CurrentUser {

	private final HttpServletRequest request;
	private final UserRepository users;

	CurrentUserImpl(HttpServletRequest request, UserRepository users) {
		this.request = request;
		this.users = users;
	}

	@Override
	public UUID requireUserId() {
		HttpSession session = request.getSession(false);
		Object userId = session == null ? null : session.getAttribute(SessionAttributes.USER_ID);
		if (userId == null) {
			throw new NotAuthenticatedException();
		}
		return (UUID) userId;
	}

	@Override
	public void requireRole(Role role) {
		UUID userId = requireUserId();
		User user = users.findById(userId).orElseThrow(NotAuthenticatedException::new);
		if (!user.roles().contains(role)) {
			throw new AccessDeniedException(role);
		}
	}
}
