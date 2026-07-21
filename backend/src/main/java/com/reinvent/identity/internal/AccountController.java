package com.reinvent.identity.internal;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/accounts")
class AccountController {

	private final IdentityService identity;

	AccountController(IdentityService identity) {
		this.identity = identity;
	}

	@GetMapping("/me")
	AccountState me(HttpSession session) {
		Object userId = session.getAttribute(SessionAttributes.USER_ID);
		if (userId == null) {
			throw new NotAuthenticatedException();
		}
		return identity.accountState((UUID) userId);
	}
}
