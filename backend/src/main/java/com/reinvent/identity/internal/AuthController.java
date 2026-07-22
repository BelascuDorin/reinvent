package com.reinvent.identity.internal;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final IdentityService identity;

	AuthController(IdentityService identity) {
		this.identity = identity;
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	AccountState signUp(@RequestBody @Valid SignupRequest request) {
		UUID userId = identity.signUp(request.email(), request.password(), request.dateOfBirth(),
				request.guardianName(), request.guardianEmail());
		return identity.accountState(userId);
	}

	@PostMapping("/login")
	AccountState login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
		UUID userId = identity.authenticate(request.email(), request.password())
				.orElseThrow(InvalidCredentialsException::new);
		// Guard against session fixation: discard any pre-authentication session
		// and start a fresh one (new id) for the signed-in User.
		HttpSession existing = httpRequest.getSession(false);
		if (existing != null) {
			existing.invalidate();
		}
		httpRequest.getSession(true).setAttribute(SessionAttributes.USER_ID, userId);
		return identity.accountState(userId);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void logout(HttpSession session) {
		session.invalidate();
	}
}
