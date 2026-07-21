package com.reinvent.identity.internal;

import jakarta.validation.constraints.NotBlank;

record LoginRequest(@NotBlank String email, @NotBlank String password) {
}
