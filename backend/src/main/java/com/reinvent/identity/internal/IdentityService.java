package com.reinvent.identity.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reinvent.identity.GuardianConsentStatus;
import com.reinvent.platform.Clock;

@Service
@Transactional
class IdentityService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	IdentityService(UserRepository users, PasswordEncoder passwordEncoder, Clock clock) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	/** Registers a new User (a Mentee by default). */
	UUID signUp(String email, String rawPassword, LocalDate dateOfBirth) {
		if (users.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException(email);
		}
		User user = new User(UUID.randomUUID(), email, passwordEncoder.encode(rawPassword), dateOfBirth, clock.now());
		return users.save(user).id();
	}

	/** @return the User's id when the credentials match, otherwise empty. */
	@Transactional(readOnly = true)
	Optional<UUID> authenticate(String email, String rawPassword) {
		return users.findByEmail(email)
				.filter(user -> passwordEncoder.matches(rawPassword, user.passwordHash()))
				.map(User::id);
	}

	@Transactional(readOnly = true)
	AccountState accountState(UUID userId) {
		User user = users.findById(userId).orElseThrow(NotAuthenticatedException::new);
		boolean minor = user.isMinorAt(clock.now());
		GuardianConsentStatus guardianConsentStatus = minor
				? GuardianConsentStatus.PENDING_GUARDIAN_CONSENT
				: GuardianConsentStatus.NOT_REQUIRED;
		return new AccountState(user.email(), List.copyOf(user.roles()), minor, guardianConsentStatus);
	}
}
