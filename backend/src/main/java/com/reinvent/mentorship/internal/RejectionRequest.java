package com.reinvent.mentorship.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A Reviewer's rejection, which must carry a reason so the decision is recorded. */
record RejectionRequest(@NotBlank @Size(max = 1000) String reason) {
}
