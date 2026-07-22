package com.reinvent.identity.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.reinvent.identity.GuardianConsentStatus;
import com.reinvent.platform.Clock;
import com.reinvent.platform.MinorMenteeRegistered;

@Service
@Transactional
class IdentityService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;
	private final ApplicationEventPublisher events;

	IdentityService(UserRepository users, PasswordEncoder passwordEncoder, Clock clock,
			ApplicationEventPublisher events) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
		this.events = events;
	}

	/**
	 * Registers a new User (a Mentee by default). A minor must name a Guardian; an
	 * adult must not. A minor lands in {@code PENDING_GUARDIAN_CONSENT} and a
	 * {@link MinorMenteeRegistered} event is published so the consent module can
	 * raise the Guardian consent request.
	 */
	UUID signUp(String email, String rawPassword, LocalDate dateOfBirth, String guardianName, String guardianEmail) {
		if (users.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException(email);
		}
		boolean minor = User.isMinorAt(dateOfBirth, clock.now());
		boolean hasGuardian = StringUtils.hasText(guardianName) && StringUtils.hasText(guardianEmail);
		if (minor && !hasGuardian) {
			throw new InvalidGuardianDetailsException("A minor must name a Guardian (name and email).");
		}
		if (!minor && (StringUtils.hasText(guardianName) || StringUtils.hasText(guardianEmail))) {
			throw new InvalidGuardianDetailsException("An adult must not name a Guardian.");
		}

		GuardianConsentStatus status = minor
				? GuardianConsentStatus.PENDING_GUARDIAN_CONSENT
				: GuardianConsentStatus.NOT_REQUIRED;
		User user = new User(UUID.randomUUID(), email, passwordEncoder.encode(rawPassword), dateOfBirth, clock.now(),
				status);
		UUID userId = users.save(user).id();

		if (minor) {
			events.publishEvent(new MinorMenteeRegistered(userId, email, guardianName, guardianEmail));
		}
		return userId;
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
		return new AccountState(user.email(), List.copyOf(user.roles()), minor, user.guardianConsentStatus());
	}
}
