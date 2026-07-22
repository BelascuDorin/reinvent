-- The consent module: one Guardian consent request per minor Mentee. The token is
-- the opaque, single-use link identifier; once consented the row is the consent
-- record itself (who/when/which minor), explicitly not identity proof (ADR-0002).
-- Expiry is derived by comparing the Clock to expires_at, so a lapsed link needs
-- no write to "become" expired.
CREATE TABLE guardian_consent
(
  token          UUID PRIMARY KEY,
  user_id        UUID                     NOT NULL,
  mentee_email   TEXT                     NOT NULL,
  guardian_name  TEXT                     NOT NULL,
  guardian_email TEXT                     NOT NULL,
  status         TEXT                     NOT NULL,
  created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
  expires_at     TIMESTAMP WITH TIME ZONE NOT NULL,
  consented_at   TIMESTAMP WITH TIME ZONE
);

-- A minor has at most one consent request in flight; look-ups by minor stay cheap.
CREATE INDEX idx_guardian_consent_user_id ON guardian_consent (user_id);
