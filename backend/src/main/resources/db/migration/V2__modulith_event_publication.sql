-- Spring Modulith's event publication registry. Cross-module communication uses
-- Modulith application events, whose delivery is made reliable by persisting each
-- publication here until its listener completes.
--
-- This is the canonical v2 Postgres schema shipped by spring-modulith-events-jdbc
-- (Modulith 2.1); the JPA registry maps onto the same table. The archive table is
-- only needed under ARCHIVE completion mode, which is not enabled.
CREATE TABLE IF NOT EXISTS event_publication
(
  id                     UUID NOT NULL,
  listener_id            TEXT NOT NULL,
  event_type             TEXT NOT NULL,
  serialized_event       TEXT NOT NULL,
  publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date        TIMESTAMP WITH TIME ZONE,
  status                 TEXT,
  completion_attempts    INT,
  last_resubmission_date TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx ON event_publication USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON event_publication (completion_date);
