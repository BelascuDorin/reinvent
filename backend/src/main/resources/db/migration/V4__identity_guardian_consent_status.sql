-- Identity's projection of where a User stands on Guardian consent. Adults are
-- NOT_REQUIRED; a minor starts PENDING_GUARDIAN_CONSENT and is flipped to CONSENTED
-- when the consent module reports the Guardian consented. The default covers the
-- (empty) existing table; the application always sets the value explicitly on insert,
-- so the default is dropped to keep it honest.
ALTER TABLE app_user
  ADD COLUMN guardian_consent_status TEXT NOT NULL DEFAULT 'NOT_REQUIRED';

ALTER TABLE app_user
  ALTER COLUMN guardian_consent_status DROP DEFAULT;
