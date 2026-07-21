-- Baseline schema for Reinvent.
--
-- Intentionally holds no domain tables yet: the walking skeleton only proves
-- that Flyway runs and owns the schema. Domain tables (User, Guardian consent,
-- Mentor application, ...) are added by later feature slices as their own
-- versioned migrations.
--
-- pgcrypto provides gen_random_uuid(), which those later tables will use for
-- primary keys.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
