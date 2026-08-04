-- A rejected User may reapply, so a User accumulates application rows and every
-- question about where they stand has to read the *current* one. That was previously
-- answered by ordering on created_at, which is not unique: two applications stamped at
-- the same instant left the choice to the database, and picking the older row makes an
-- approved Mentor look rejected.
--
-- Record which attempt an application is instead. It is real domain data ("your second
-- application"), it does not depend on clock resolution, and it gives history a total
-- order per User.
ALTER TABLE mentor_application
  ADD COLUMN attempt INTEGER;

-- Backfill existing rows in the order they were created; created_at is enough here
-- because this runs once over data that predates the column, with id breaking any tie.
UPDATE mentor_application AS target
SET attempt = ordered.attempt
FROM (SELECT id,
             ROW_NUMBER() OVER (PARTITION BY applicant_user_id ORDER BY created_at, id) AS attempt
      FROM mentor_application) AS ordered
WHERE target.id = ordered.id;

ALTER TABLE mentor_application
  ALTER COLUMN attempt SET NOT NULL;

-- Two applications by one User can never be the same attempt. A database backstop to
-- the service's count, in the same spirit as the one-open-application index above it.
CREATE UNIQUE INDEX uq_mentor_application_attempt_per_user
  ON mentor_application (applicant_user_id, attempt);
