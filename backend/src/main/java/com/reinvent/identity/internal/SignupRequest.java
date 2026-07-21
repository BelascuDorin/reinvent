package com.reinvent.identity.internal;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

record SignupRequest(
		@Email @NotBlank String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotNull @Past LocalDate dateOfBirth) {
}
