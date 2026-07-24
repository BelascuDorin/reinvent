# Spec: Mentor discovery (Fields, Mentor profiles, browse & search)

> Status: ready-for-agent · Scope: the Discovery slice — curated **Fields**, public
> **Mentor profiles**, and browse/search by Field, plus the Mentor-side setup of
> their profile. Builds directly on spec 0001 (accounts, Mentor application,
> Reviewer approval). Uses [CONTEXT.md](../../CONTEXT.md) domain language throughout
> and respects ADRs 0001–0004. Slots, booking, payments, video, and reviews are
> explicitly out of scope for later specs.

## Problem Statement

After spec 0001, Reinvent can turn a person into a **User**, gate minor **Mentees**
behind **Guardian consent**, and let a **Reviewer** approve a **Mentor** — but the
core value of the product still doesn't exist: a teenager exploring a career path
**cannot find a Mentor**.

- A **Mentee** has no way to browse or search Mentors, and nothing to browse by —
  there is no notion of a **Field** in the running system.
- An approved **Mentor** has no **Mentor profile**: no way to present who they are,
  what **Field(s)** they practise, their price, or their **Meeting duration**. Being
  approved is invisible to the outside world.
- None of Reinvent's surfaces are yet the public, search-indexable pages the SSR
  foundation (Nuxt) was chosen to enable — so there is no organic discovery.

Without discovery, approval is a dead end: a Mentor is vetted but undiscoverable,
and a Mentee has nobody to consider. This slice makes the marketplace's supply side
**visible** so that the next slice (booking) has something to book against.

## Solution

Give approved Mentors a **Mentor profile** and give Mentees a way to **discover**
them by **Field**.

From the user's perspective:

- A **Mentor** (a User the Reviewer has approved) gets a **Mentor profile** to fill
  in: display name, current role/title, one or more **Field(s)**, bio, experience,
  languages, employer (optional), their price, and their **Meeting duration**. The
  profile is seeded as a draft from their application when they are approved, so
  there is something to start from.
- A **Mentor** only becomes **discoverable** once their profile is *complete* (at
  least one Field, a price, and a Meeting duration) and they are approved and not
  suspended. Completing the profile is what puts them in front of Mentees; a
  suspended Mentor disappears from discovery without losing their profile data.
- A **Mentee** (or any visitor) can browse the curated list of **Fields**, search /
  filter Mentors by Field, and open a **Mentor profile** to read the full public
  presentation. These pages are server-rendered so they are shareable and
  search-indexable.
- A visitor sees a Mentor's **rating summary** slot on the profile, empty for now —
  the place the later Reviews slice fills in.

Underneath, this slice extends the existing **`mentorship`** module to own Mentor
profiles, the curated Field list, and the discovery read-side — no new module, no
new ports. Public discovery pages continue to reach the backend only through the
Nuxt server routes (BFF), never browser→backend directly.

## User Stories

### Discovering Mentors (Mentee / visitor)

1. As a visitor, I want to see the curated list of **Fields**, so that I know which
   career areas I can explore on Reinvent.
2. As a Mentee, I want to browse Mentors filtered by a **Field**, so that I can find
   people who practise the career path I'm considering.
3. As a Mentee, I want the Field list to be a curated, controlled set rather than
   free text, so that discovery has a single reliable axis and I'm not guessing at
   spellings or synonyms.
4. As a Mentee, I want to open a **Mentor profile** and see the Mentor's display
   name, current role/title, Field(s), bio, experience, languages, price, and
   Meeting duration, so that I can decide whether they're worth meeting.
5. As a Mentee, I want to see a Mentor's employer only when they've chosen to show
   it, so that the profile respects what the Mentor opted to share.
6. As a Mentee, I want to see a Mentor's price and Meeting duration up front, so
   that I know the cost and commitment before I ever try to book.
7. As a Mentee, I want a place on the profile for the Mentor's **rating summary**,
   so that once Reviews exist I can factor reputation in (empty for now).
8. As a visitor, I want Mentor profiles and browse pages to be real shareable URLs
   that render server-side, so that I can find Mentors via search engines and share
   a profile link.
9. As a Mentee, I want a Mentor who has been **suspended** to not appear in
   discovery, so that I'm never shown someone who's been taken out of circulation.
10. As a Mentee, I want to only ever see Mentors whose profile is complete enough to
    consider (has a Field, a price, and a Meeting duration), so that I don't land on
    half-finished profiles.
11. As a Mentee, I want a Mentor who hasn't finished onboarding to still be
    understandable as "not yet bookable" when booking exists, without discovery
    pretending they're absent — but for this slice, discovery lists approved,
    complete, non-suspended Mentors regardless of payment onboarding.
12. As a visitor, I want browsing and viewing profiles to require no account, so
    that I can explore Reinvent before committing to sign up.
13. As a Mentee, I want to filter Mentors by language as well as Field, so that I
    can find a Mentor I can actually converse with (secondary filter).
14. As a Mentee, I want a Field with no discoverable Mentors to show an empty state
    rather than an error, so that I understand it's simply unfilled, not broken.

### Managing my Mentor profile (Mentor)

15. As a newly approved Mentor, I want a draft profile created from my application,
    so that I'm not starting from a blank page.
16. As a Mentor, I want to edit my profile — display name, role/title, bio,
    experience, languages, employer, price, Meeting duration, and Field(s) — so that
    I control how I'm presented.
17. As a Mentor, I want to choose my Field(s) from the curated list, so that I'm
    discoverable under the right career areas.
18. As a Mentor, I want to set a single **Meeting duration** that applies to all my
    future Slots, so that my offering is consistent (as the glossary defines).
19. As a Mentor, I want to set my own price, so that I decide what my time costs.
20. As a Mentor, I want to see whether my profile is complete and therefore
    discoverable, so that I know what's left to do to appear in search.
21. As a Mentor, I want my profile to go live in discovery automatically once it's
    complete (and I'm approved and not suspended), so that there's no extra publish
    step to forget.
22. As a Mentor, I want to preview my own public profile, so that I can see what
    Mentees will see.
23. As a Mentor, I want to leave employer blank, so that I can mentor without
    disclosing where I work.
24. As a suspended Mentor, I want my profile data kept while I'm hidden from
    discovery, so that if my suspension is lifted I don't have to rebuild it.

### Authorization & integrity

25. As a User without the Mentor role, I want to be refused when I try to create or
    edit a Mentor profile, so that only Mentors present themselves as Mentors.
26. As a User, I want only my own Mentor profile to be editable by me, so that no
    one can alter another Mentor's presentation.
27. As any actor, I want a Field reference that can't be set to a value outside the
    curated list, so that discovery's primary axis stays clean.
28. As an operator, I want the curated Field list managed by versioned migration
    (seeded), so that the controlled vocabulary is reproducible across environments
    and changeable without a code redeploy.

## Implementation Decisions

### Module & seam

- **Extend the existing `mentorship` module** to own Mentor profiles, the curated
  Field list, and the discovery read-side. **No new module and no new ports.** A
  dedicated `discovery`/`catalog` module was considered and rejected: it would have
  to read `mentorship`'s profile data across the module boundary, adding a second
  test seam and a cross-module dependency for no benefit at this size. Keeping
  everything in `mentorship` keeps the behavioral seam count at one.
- **The Mentor profile stores its own display name** (and all public presentation
  fields). Discovery therefore never reaches into the `identity` module for user
  data — there is no cross-module read. `identity` remains the source of the login
  identity; `mentorship` owns the public presentation.

### Domain model & rules

- **Mentor profile** — a new aggregate in `mentorship`, one per Mentor (keyed by the
  Mentor's User id). Fields: display name, role/title (headline), bio, experience,
  languages (a set), employer (optional/nullable), price (amount + currency),
  **Meeting duration** (a single fixed length in minutes, per the glossary), and the
  Mentor's chosen **Field(s)** (one or more). Holds a slot for a **rating summary**
  that stays empty until the Reviews slice populates it.
- **Field** — a curated, controlled reference list, **seeded via Flyway** into its own
  table (e.g. slug + display name + sort order), not a Java enum, so the vocabulary
  is business-configurable without a redeploy. A profile references Fields through a
  join; a Field reference outside the curated set is rejected.
- **Profile provisioning on approval** — when a Reviewer approves a Mentor
  application, `mentorship` creates a **draft** Mentor profile seeded from the
  application (headline/bio). This is an **intra-module** reaction to `mentorship`'s
  own approval — **no new cross-module event** and no change to the `platform`
  shared kernel. (The existing `MentorApproved` event to `identity` for the role
  grant is unchanged.)
- **Completeness gates visibility** — a Mentor is **discoverable** iff:
  `application APPROVED ∧ ¬suspended ∧ profile complete`, where *complete* means at
  least one Field, a price, and a Meeting duration are set. There is **no separate
  publish flag** — profile completeness is the switch. Editing a profile back to
  incomplete, or being suspended, removes the Mentor from discovery without
  destroying data.
- **Relationship to `MentorBookability`** — discovery visibility is deliberately a
  *weaker* predicate than the existing `MentorBookability` (which additionally
  requires `PaymentGateway.isPayoutOnboardingComplete`). Discovery lists approved,
  complete, non-suspended Mentors regardless of payment onboarding; the payment gate
  is a booking-time concern for the next slice. `MentorBookability` is left as-is,
  still unconsumed, for the booking spec.

### API contracts (shape, not paths)

- **Fields (public, unauthenticated):** list the curated Fields.
- **Discovery (public, unauthenticated):** list/search Mentors, filterable by Field
  (primary) and language (secondary), returning profile summaries (display name,
  role/title, Field(s), price, Meeting duration, languages, rating-summary
  placeholder). Only discoverable Mentors (approved ∧ complete ∧ ¬suspended) are
  returned. An empty result is a valid empty list, not an error.
- **Public Mentor profile (public, unauthenticated):** fetch one Mentor's full
  public profile by a stable identifier.
- **Mentor profile — owner (authenticated, MENTOR role):** read my own profile
  (including completeness/discoverability status) and update it (display name,
  role/title, bio, experience, languages, employer, price, Meeting duration,
  Field(s)). Guarded so only a User with the MENTOR role can reach it and only the
  owner can edit their own profile — reusing the `CurrentUser` session guard from
  spec 0001 (`requireUserId` / `requireRole`), not Spring Security.
- All request/response bodies use CONTEXT.md vocabulary (Mentor, Mentee, Field,
  Mentor profile, Meeting duration) — never "category", "tag", "listing", "account".

### Schema changes (Flyway)

- New `field` reference table, **seeded** with an initial curated list (e.g.
  Software Engineering, Medicine, Law, Finance, Design, …).
- New `mentor_profile` table keyed by the Mentor's User id, holding the presentation
  fields, price (amount + currency), and Meeting duration.
- New join table linking `mentor_profile` to `field` (a Mentor may practise several
  Fields).
- Follows spec 0001's convention: **Flyway owns the schema** (`ddl-auto=none`), one
  migration per change, continuing the `V8…` sequence.

### Frontend (Nuxt, SSR)

- New **public, server-rendered** pages: a browse/search page (by Field, with a
  language filter) and a Mentor profile page — both indexable, both reaching the
  backend only through **BFF** server routes (`server/api/**`), never
  browser→backend, consistent with spec 0001.
- The existing Mentor area gains a **profile editor** for the signed-in Mentor plus
  a preview of their own public profile and a clear "what's needed to be
  discoverable" indicator.

## Testing Decisions

- **What makes a good test here:** it drives *external behavior* through the
  `mentorship` module's public seam and asserts observable outcomes — reads as an
  actor story ("an approved Mentor with a complete profile appears in discovery for
  their Field"; "a suspended Mentor disappears from discovery"; "a non-Mentor is
  refused the profile editor") — never asserting private state, SQL, or internal
  calls.
- **Primary seam:** the **`mentorship` module's public API + HTTP boundary** — the
  *same single seam* established in spec 0001. Extend `MentorshipModuleTest`
  (`@ApplicationModuleTest`) for module-level behavior and follow the
  `MentorApplicationFlowTests` pattern (`@SpringBootTest` + HTTP, real session) for
  the authorization and end-to-end discovery flows. No new test seam is introduced.
- **Real dependencies, faked edges:** run against **real Postgres via
  Testcontainers** (so the seeded Field table and the join queries are exercised as
  in production). No new ports; existing port fakes (`FakeClock`, etc.) are used only
  where a timestamp is needed. Payment/video/notifier ports are untouched by this
  slice.
- **Behaviors to cover:**
  - Discovery returns only Mentors that are `APPROVED ∧ ¬suspended ∧ complete`.
  - Completing a draft profile makes a Mentor appear; suspending removes them;
    editing back to incomplete removes them.
  - Filtering by Field (and by language) returns the right Mentors; an unfilled
    Field returns an empty list, not an error.
  - Only the MENTOR role can reach the profile editor, and a Mentor can edit only
    their own profile (authz, via `CurrentUser`).
  - A Field reference outside the curated list is rejected.
  - Approval provisions a draft profile seeded from the application.
  - Public discovery/profile reads require no authentication.
- **Prior art:** `MentorshipModuleTest` and `MentorApplicationFlowTests` (spec 0001,
  ticket #5) are the direct templates; `IdentityHttpTests` for the unauthenticated
  vs authenticated HTTP distinction. The Testcontainers + `@ApplicationModuleTest` +
  faked-ports pattern from spec 0001 is the reference.

## Out of Scope

- **Slots, Meeting duration as bookable windows, and the Slot→Meeting booking
  flow** — this slice sets a Mentor's Meeting duration and price as *profile* data
  only; opening Slots and booking are the next spec.
- **Payments** — no Stripe Connect charging, escrow, Payouts, commission, or
  payment-onboarding gating of discovery. `MentorBookability` stays unconsumed.
- **Video / Meeting link / Meeting brief.**
- **Reviews and Reports** — the rating summary is a placeholder only; no rating
  computation, no review submission, no reporting.
- **Relevance ranking / full-text search / search infrastructure** (e.g.
  Elasticsearch) — discovery is filter-by-Field (and language), with a simple,
  stable ordering. No fuzzy matching or scoring.
- **Guardian-facing surfaces** — discovery is Mentee/visitor facing; no Guardian
  involvement in this slice.
- **Mentor profile media** (photos/avatars, video intros) and rich formatting —
  text fields only.
- **Editing the Field vocabulary through an admin UI** — Fields are managed by
  seeded migration for now; a Reviewer/admin curation UI is a later concern.

## Further Notes

- This is spec **0002** of the series; spec 0001 (walking skeleton & accounts) is its
  foundation and is complete. It respects ADR-0001 (only approved Mentors are
  discoverable), ADR-0002/0003 (discovery does not bypass the consent or payment
  gates — it only makes supply visible), and ADR-0004 (no messaging surface is
  introduced; discovery is read-only presentation).
- The next spec (**0003**) is expected to be **booking** — Slots, the Slot→Meeting
  flow, Stripe Connect escrow charge-at-booking (consuming `MentorBookability` and
  the `PaymentGateway` port), the Jitsi Meeting link (`VideoProvider`), Guardian-as-
  Payer, and the Meeting brief. Discovery deliberately establishes the Mentor
  profile (price, Meeting duration, Field) that booking will read.
- **Reviews/Reports** come after booking (they need completed Meetings); the
  rating-summary placeholder on the profile is the seam they will fill.
