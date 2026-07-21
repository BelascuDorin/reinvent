# Minors are gated by one-time Guardian consent, and the Guardian is the Payer

Mentees are teenagers, so a minor Mentee cannot book Meetings until a named
Guardian gives a single, platform-level consent via a link (confirming
guardianship and consent to use Reinvent). For minor Mentees the Guardian — not
the Mentee — is the Payer whose payment method is charged. Consent is treated as
a consent record, not proof of identity.

## Considered Options

- **No age handling** — rejected; assumes minors can independently contract with and pay strangers.
- **Age-gated self-consent, no guardian** — rejected; ignores that guardians must consent and that minors rarely hold payment methods.
- **Per-Meeting guardian consent** — rejected as redundant given every Mentor is already manually vetted (ADR-0001); replaced by one-time consent plus per-booking notifications to the Guardian.

## Consequences

- Booking depends on the Guardian being reachable and having a payment method on file.
- Adult Mentees (18+) act without a Guardian and pay for themselves.
