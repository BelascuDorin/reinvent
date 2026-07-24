-- The Mentor profile (spec 0002): the public presentation an approved Mentor fills
-- in, one per Mentor, keyed by their User id. mentorship owns this data itself (its
-- own display name and all presentation fields) so discovery never reads across into
-- identity. A draft row is provisioned on approval; all presentation fields start
-- empty and the Mentor fills them in later (spec 0002 ticket #9 editor).
--
-- Price is stored as amount + currency; Meeting duration is a single fixed length in
-- minutes (glossary). Completeness — at least one Field, a price, and a Meeting
-- duration — is what gates discovery visibility; there is no separate publish flag.
CREATE TABLE mentor_profile
(
  mentor_user_id           UUID PRIMARY KEY,
  display_name             TEXT,
  role_title               TEXT,
  bio                      TEXT,
  experience               TEXT,
  employer                 TEXT,
  price_amount             NUMERIC(19, 2),
  price_currency           TEXT,
  meeting_duration_minutes INTEGER,
  created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at               TIMESTAMP WITH TIME ZONE NOT NULL
);

-- A Mentor may practise several Fields; each reference must be a curated Field.
CREATE TABLE mentor_profile_field
(
  mentor_user_id UUID NOT NULL REFERENCES mentor_profile (mentor_user_id),
  field_slug     TEXT NOT NULL REFERENCES field (slug),
  PRIMARY KEY (mentor_user_id, field_slug)
);

-- Languages the Mentor can converse in — a secondary discovery filter.
CREATE TABLE mentor_profile_language
(
  mentor_user_id UUID NOT NULL REFERENCES mentor_profile (mentor_user_id),
  language       TEXT NOT NULL,
  PRIMARY KEY (mentor_user_id, language)
);
