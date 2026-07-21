-- The identity module: the single User account and the roles it holds.
-- "user" is reserved in Postgres, so the table is app_user.
CREATE TABLE app_user
(
  id            UUID PRIMARY KEY,
  email         TEXT                     NOT NULL UNIQUE,
  password_hash TEXT                     NOT NULL,
  date_of_birth DATE                     NOT NULL,
  created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

-- A User may hold several roles (everyone is MENTEE by default; MENTOR and
-- REVIEWER are added later). Stored as its own table rather than a column so a
-- User can hold both MENTEE and MENTOR.
CREATE TABLE user_role
(
  user_id UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
  role    TEXT NOT NULL,
  PRIMARY KEY (user_id, role)
);
