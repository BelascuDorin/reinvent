-- A Mentor application now carries what the applicant says about themselves: a headline
-- and a bio. Two reasons, both from the specs:
--
--   * ADR-0001 makes approval a manual, human decision — a Reviewer vetting an
--     application had nothing to read but a User id.
--   * Spec 0002 seeds a Mentor's draft profile from their application so an approved
--     Mentor doesn't start from a blank page (story 15). Nothing existed to seed from.
--
-- Nullable on purpose: applications submitted before this column existed genuinely have
-- no headline or bio, and inventing a placeholder for them would be a lie. New
-- applications are required to carry both, enforced at the request boundary where the
-- applicant can be told what is missing.
ALTER TABLE mentor_application
  ADD COLUMN headline TEXT,
  ADD COLUMN bio      TEXT;
