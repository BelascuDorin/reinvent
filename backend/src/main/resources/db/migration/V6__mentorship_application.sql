-- The mentorship module: a User's application to take on the Mentor role and its
-- lifecycle (ADR-0001). Suspension is a flag on an approved application, not a
-- lifecycle status. History is kept (a rejected applicant may reapply), so the
-- applicant_user_id is not unique on its own.
CREATE TABLE mentor_application
(
  id                UUID PRIMARY KEY,
  applicant_user_id UUID                     NOT NULL,
  status            TEXT                     NOT NULL,
  rejection_reason  TEXT,
  suspended         BOOLEAN                  NOT NULL,
  created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at        TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Queue reads are by status, ordered by age.
CREATE INDEX idx_mentor_application_status ON mentor_application (status, created_at);

-- Enforce "at most one open or approved application per User" at the database, as a
-- backstop to the service check: a User may hold only one non-rejected application.
CREATE UNIQUE INDEX uq_mentor_application_open_per_user
  ON mentor_application (applicant_user_id)
  WHERE status <> 'REJECTED';
