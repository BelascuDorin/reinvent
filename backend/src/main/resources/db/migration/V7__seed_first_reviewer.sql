-- Bootstrap the first Reviewer out-of-band: there is no self-serve path to the
-- internal REVIEWER role (ADR-0001), so the first one is seeded here.
--
-- SECURITY: this ships a known bootstrap credential. Before any real deployment,
-- rotate this password (or delete this account and provision Reviewers through a
-- proper admin flow). Login email: reviewer@reinvent.example — the password hash
-- below is BCrypt for a dev-only password.
INSERT INTO app_user (id, email, password_hash, date_of_birth, created_at, guardian_consent_status)
VALUES (
  '00000000-0000-0000-0000-000000000001',
  'reviewer@reinvent.example',
  '$2y$10$1J9HRTj5CLugNpKGJ6emRuw8bbwL35T7qzvJdI5ugCQ.IcN/f4b8G',
  DATE '1980-01-01',
  TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00',
  'NOT_REQUIRED'
);

INSERT INTO user_role (user_id, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'REVIEWER');
