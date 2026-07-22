package com.reinvent.identity.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reinvent.identity.CurrentUser;

@RestController
@RequestMapping("/api/accounts")
class AccountController {

	private final IdentityService identity;
	private final CurrentUser currentUser;

	AccountController(IdentityService identity, CurrentUser currentUser) {
		this.identity = identity;
		this.currentUser = currentUser;
	}

	@GetMapping("/me")
	AccountState me() {
		return identity.accountState(currentUser.requireUserId());
	}
}
