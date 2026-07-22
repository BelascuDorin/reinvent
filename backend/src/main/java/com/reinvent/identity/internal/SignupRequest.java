package com.reinvent.identity.internal;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

/**
 * Signup payload. The Guardian fields are optional at the bean-validation layer
 * because whether they are required depends on the applicant's age, which is only
 * known once the {@link com.reinvent.platform.Clock} is consulted: a minor must
 * name a Guardian and an adult must not. {@link IdentityService} enforces that rule.
 */
record SignupRequest(
		@Email @NotBlank String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotNull @Past LocalDate dateOfBirth,
		@Size(max = 200) String guardianName,
		@Email @Size(max = 254) String guardianEmail) {
}
