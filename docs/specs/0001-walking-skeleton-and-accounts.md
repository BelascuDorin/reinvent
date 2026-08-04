# Spec: Walking skeleton & accounts foundation

> Status: ready-for-agent · Scope: foundational slice (accounts, roles, mentor
> application review, guardian consent) + the architectural seams the rest of
> Reinvent will be built through. Uses the [CONTEXT.md](../../CONTEXT.md) domain
> language throughout and respects ADRs 0001, 0002, 0004.

## Problem Statement

Reinvent has a domain model but no running system. A teenager exploring a career
path cannot yet create an account, an adult cannot yet offer to mentor, and staff
have no way to vet anyone. Before any of Reinvent's value (discovering and meeting
Mentors) can exist, the platform needs the identity and trust foundation the whole
product sits on:

- A person cannot become a **User** at all.
- A minor **Mentee** has no way for their **Guardian** to consent, so the safety
  gate that lets minors use the product does not exist.
- Someone willing to mentor cannot submit a **Mentor application**, and a
  **Reviewer** has no way to approve, reject, or suspend anyone — so the manual
  vetting that ADR-0001 requires before an adult can be booked by a minor is
  absent.

Equally, there is no codebase: no module boundaries, no test seams, and no
adapters for the external systems (payments, video, guardian notification, time)
that later features depend on. Without that skeleton, every later feature spec
would have to re-litigate the architecture.

## Solution

Stand up a **walking skeleton** — a thin, end-to-end running system — and fill in
the **accounts** slice on top of it.

From the user's perspective:

- Anyone can sign up and become a **User**. Everyone is a **Mentee** by default.
- At signup a Mentee states their date of birth. A **minor** Mentee names a
  **Guardian**; the Guardian receives a consent link and, in one action, gives
  platform-level **Guardian consent**. Until then the minor Mentee sits in
  *pending guardian consent* and is blocked from the actions later specs will
  gate (e.g. booking). An **adult** Mentee needs no Guardian.
- A User can submit a **Mentor application**. It moves `applied → under review →
  approved / rejected`. A **Reviewer** (staff) sees the queue and approves or
  rejects applications, and can **suspend** an approved Mentor.
- The system records — but does not yet act on — a Mentor's payment-onboarding
  status, so the "a Mentor is bookable only when approved *and* payment-onboarded"
  rule (ADR-0003) has a home to grow into.

Underneath, the solution establishes the architecture every later spec inherits: a
**Spring Modulith modular monolith** (Java 25 LTS, Spring Boot 3) over **Postgres**,
a **Nuxt 3 (Vue 3 + SSR)** frontend, and a small set of **ports** for the outside
world (payments, video, guardian notification, clock) that are faked in tests and
stubbed in this slice.

## User Stories

### Becoming a User / Mentee

1. As a visitor, I want to sign up with an email and password, so that I become a
   User of Reinvent.
2. As a new User, I want to be a Mentee by default without any extra step, so that
   I can start using the product immediately as the teenager the product is for.
3. As a signing-up User, I want to state my date of birth, so that Reinvent can
   tell whether I am a minor and apply the right safety rules.
4. As a returning User, I want to log in and stay signed in across page loads, so
   that I don't have to re-authenticate constantly.
5. As a User, I want to log out, so that I can end my session on a shared device.
6. As a User, I want to see my own account state (my roles, and if I'm a minor,
   my guardian-consent status), so that I understand what I can and cannot do yet.
7. As a User, I want signup to reject an email that already has an account, so that
   there is one identity per person.
8. As a User, I want to be prevented from ever being both parties of a future
   Meeting with myself, so that the "cannot book a Meeting with themselves" rule
   holds — established here as an identity invariant even before booking exists.

### Minor Mentee & Guardian consent

9. As a minor Mentee, I want to name my Guardian (name + email) at signup, so that
   the person who must consent on my behalf is on record.
10. As a minor Mentee, I want to land in a clear *pending guardian consent* state
    after signup, so that I understand I can't yet do the gated actions and why.
11. As a Guardian, I want to receive a consent link (not a Reinvent account), so
    that I can act without becoming a User myself.
12. As a Guardian, I want the consent link to show which minor and what I'm
    consenting to, so that I can make an informed decision.
13. As a Guardian, I want to give consent in one action confirming both that I am
    the Mentee's guardian and that I consent to their use of Reinvent, so that the
    one-time, platform-level consent of ADR-0002 is captured.
14. As a minor Mentee, I want my state to move to *consented* once my Guardian
    acts, so that the gate the rest of the product checks is lifted.
15. As a minor Mentee, I want a consent link that is single-purpose and not
    reusable to consent for someone else, so that the consent record is trustworthy.
16. As a Guardian, I want an expired or already-used consent link to fail safely
    rather than silently do nothing, so that I know to request a fresh one.
17. As an adult Mentee (18+), I want to skip guardian consent entirely, so that I
    act for myself as ADR-0002 states.
18. As a minor Mentee, I want the system to treat consent as a consent record, not
    proof of my or my Guardian's identity, so that expectations match ADR-0002.

### Mentor application

19. As a User, I want to apply to take on the Mentor role, so that I can offer my
    time to be met with.
20. As a Mentor applicant, I want my application to start in *applied*, so that it
    enters the review pipeline.
21. As a Mentor applicant, I want to see my application's current status (applied /
    under review / approved / rejected), so that I know where I stand.
22. As a User, I want to be stopped from having more than one open Mentor
    application at a time, so that the review queue isn't duplicated.
23. As a rejected applicant, I want to know I was rejected, so that I'm not left
    waiting indefinitely.
24. As a newly approved Mentor, I want to be told I still can't be booked until I
    complete payment onboarding, so that I understand the second gate from ADR-0003.

### Reviewer

25. As a Reviewer, I want to see the queue of Mentor applications, so that I can
    work through vetting.
26. As a Reviewer, I want to move an application to *under review* when I pick it
    up, so that work isn't duplicated with other Reviewers.
27. As a Reviewer, I want to approve an application, so that the User gains the
    Mentor role.
28. As a Reviewer, I want to reject an application with a reason, so that the
    decision is recorded.
29. As a Reviewer, I want to suspend an approved Mentor, so that a Mentor who
    becomes unsafe can be taken out of circulation.
30. As a Reviewer, I want only Reviewers (not Mentors or Mentees) to reach the
    review actions, so that vetting stays an internal, trusted function.
31. As a Reviewer, I want application state transitions to be legal-only (e.g. I
    can't approve something already rejected), so that the lifecycle can't be
    corrupted.

### Roles & authorization

32. As a User, I want to hold the Mentee role, the Mentor role, or both, so that
    one identity covers both sides of the marketplace.
33. As a User who is approved as a Mentor, I want to keep my Mentee capabilities,
    so that becoming a Mentor is additive, not a switch.
34. As any actor, I want actions I'm not authorized for to be refused, so that the
    role boundaries (Mentee / Mentor / Reviewer) are enforced, not cosmetic.

### Platform / operator (skeleton)

35. As an operator, I want a health/readiness endpoint, so that I can tell the
    system is up.
36. As an operator, I want database schema managed by versioned migrations, so
    that environments are reproducible.
37. As a developer, I want each domain module to expose a narrow public API and
    keep its internals private, so that modules can be understood and tested in
    isolation.
38. As a developer, I want the external world (payments, video, guardian
    notification, time) behind ports with fakes, so that flows are testable
    without real external calls or wall-clock time.
39. As a developer, I want a Nuxt page that exercises the real backend end-to-end
    (sign up → see account state), so that the skeleton genuinely walks.

## Implementation Decisions

### Architecture & stack

- **Backend: Java 25 LTS + Spring Boot 3.x**, structured as a **Spring Modulith
  modular monolith**. Chosen over Node/TS and plain Java layering because the
  domain is money- and safety-critical (transactional integrity for later
  escrow/booking) and already modeled DDD-style; Modulith enforces the bounded-
  context boundaries the glossary implies and gives a per-module test seam.
  Kotlin was considered (null-safety, sealed classes) and deferred in favour of
  the team's Java familiarity; Java 25 records + sealed interfaces cover the
  state-machine modeling need.
- **Persistence: PostgreSQL**, schema via **Flyway** migrations. Relational
  integrity is non-negotiable for later money movement.
- **Frontend: Nuxt 3 (Vue 3, Composition API, TypeScript, Pinia).** Vue confirmed;
  Nuxt chosen over a plain Vue SPA so that later public surfaces (Mentor profiles,
  Field discovery) are SSR/SSG and search-indexable for organic discovery. This
  slice ships only the auth + account-state pages but on the SSR foundation.
- **Modules (initial):** `identity` (User, auth, sessions, roles), `mentorship`
  (Mentor application lifecycle, Reviewer actions, suspension), `consent`
  (Guardian, Guardian consent records + link flow). Cross-module communication is
  via published module APIs / Spring Modulith application events, never reaching
  into another module's internals.

### Ports (the seams to the outside world)

Declared now, with fakes for tests and stubs/no-op-or-config-driven impls for this
slice, so later specs implement them without re-architecting:

- `PaymentGateway` — represents Stripe Connect onboarding + charging. In this slice
  it only records/reports a Mentor's onboarding status (used by the bookability
  predicate); no real charges.
- `VideoProvider` — produces a Meeting link (Jitsi at launch, per ADR-0004). Not
  exercised in this slice; the port exists so the domain never depends on a
  provider.
- `GuardianNotifier` — sends the Guardian consent link (and later booking
  notifications). Fake in tests captures messages; a simple impl is acceptable for
  this slice.
- `Clock` — the single source of "now". No domain code reads wall-clock time
  directly, so consent expiry and future dispute/payout windows are deterministic
  in tests.

### Domain model & rules

- **User** is the single identity. Roles: `MENTEE` (default for all), `MENTOR`
  (granted only on Reviewer approval), `REVIEWER` (internal). A User may hold both
  MENTEE and MENTOR. Identity carries enough to enforce "a User cannot be both
  parties of a Meeting" as an invariant later.
- **Age**: derived from date of birth via `Clock`. A Mentee is a *minor* if under
  18 at signup evaluation. Minors require a named Guardian; adults must not have
  one.
- **Guardian consent** state on a minor Mentee: `PENDING_GUARDIAN_CONSENT →
  CONSENTED`. One-time, platform-level, not per-Meeting (ADR-0002). Modeled as a
  consent record (who consented, when, for which minor), explicitly not identity
  proof. Consent link is single-use, single-purpose, and expires (expiry via
  `Clock`); used/expired links fail explicitly.
- **Mentor application** lifecycle as a sealed/enum state machine: `APPLIED →
  UNDER_REVIEW → APPROVED | REJECTED`. Only legal transitions permitted. Rejection
  carries a reason. Approval grants the MENTOR role. At most one open application
  per User.
- **Suspension**: a Reviewer can suspend an approved Mentor; a suspended Mentor is
  not bookable. Suspension is distinct from application state.
- **Bookability predicate** (defined, not yet consumed): a Mentor is bookable only
  when application is `APPROVED`, not suspended, **and** `PaymentGateway` reports
  onboarding complete (ADR-0003). Booking itself is out of scope; the predicate is
  established so later specs plug in.

### API contracts (shape, not paths)

- **Auth**: sign up (email, password, date of birth, and — for minors — guardian
  name + email); log in; log out; get current account (roles, and for minors the
  guardian-consent status). Session-based auth (HTTP-only cookie); Nuxt SSR reads
  the session server-side.
- **Consent**: resolve a consent link to its context (minor + what is being
  consented to); submit consent (rejects reused or expired links explicitly —
  see the amendment below).
- **Mentor application**: submit application; get my application status.
- **Reviewer**: list application queue; transition an application (pick up →
  under review, approve, reject-with-reason); suspend a Mentor. Guarded to the
  REVIEWER role.

All request/response bodies use the CONTEXT.md vocabulary (Mentee, Mentor, Guardian,
Guardian consent, Mentor application, Reviewer, etc.) — never "account", "member",
"admin", "parent".

### Out-of-band setup

- The first **Reviewer** is provisioned out-of-band (seed migration or admin
  bootstrap), since there is no self-serve path to the internal role.

## Testing Decisions

- **What makes a good test here:** it drives *external behavior* through a module's
  public seam and asserts observable outcomes — not internal method calls, private
  state, or persistence details. Tests read as user/actor stories ("a minor whose
  Guardian consents becomes CONSENTED", "approving an application grants the MENTOR
  role", "a Mentee cannot reach Reviewer actions").
- **Primary seam:** each domain module's public API, exercised via Spring Modulith
  `@ApplicationModuleTest` and/or the HTTP boundary (`@SpringBootTest` +
  MockMvc/TestRestTemplate). This keeps the seam count near one — one behavioral
  seam per module — rather than testing internals.
- **Real dependencies, faked edges:** tests run against a **real Postgres via
  Testcontainers** (no in-memory substitute that diverges from prod). The only
  test doubles are the four **ports** (`PaymentGateway`, `VideoProvider`,
  `GuardianNotifier`, `Clock`) — fakes that capture calls and make time/consent-
  expiry deterministic.
- **Modules tested:** `identity` (signup/login/roles/session, single-identity
  invariant), `consent` (minor gating, consent link single-use/expiry, adult
  bypass), `mentorship` (application lifecycle legal transitions, approval grants
  role, Reviewer-only authorization, suspension).
- **Frontend:** one end-to-end happy path through the Nuxt SSR app against the real
  backend (sign up → view account state) proving the skeleton walks; component-level
  detail is out of scope for this slice.
- **Prior art:** none yet — this spec establishes the prior art. The Testcontainers
  + `@ApplicationModuleTest` + faked-ports pattern set here is the reference every
  later feature spec should follow.

## Out of Scope

- **Discovery**: Fields, Mentor profiles, search/browse.
- **Slots, Meeting duration, and the Slot→Meeting booking flow.**
- **Payments beyond the onboarding-status stub**: no real Stripe Connect charging,
  escrow, Payouts, Platform commission, refunds, or no-show handling.
- **Video**: no real Jitsi Meeting links or the Meeting brief.
- **Reviews and Reports** (and the dispute/suspension flow driven by a Report).
- **Booking notifications to the Guardian** (only the consent-link notification is
  in scope).
- **Rich Mentor profile fields** (bio, languages, price, employer) beyond what the
  application/role model needs.
- **Password reset, email verification, MFA, social login** — basic email/password
  auth only for the skeleton.

## Further Notes

- This is spec **0001** of a series. It deliberately front-loads architecture (the
  modular monolith, the ports/seams, the SSR foundation) so later specs
  (discovery, booking, payments, video, reviews/reports) are additive and inherit
  the test seams rather than reinventing them.
- Every later spec that touches money or minors must respect the ADRs: 0001 (manual
  approval), 0002 (guardian consent + Payer), 0003 (Stripe Connect escrow), 0004
  (video-only, no free-form messaging).
- The bookability predicate and the four ports are intentionally defined ahead of
  their consumers — they are the extension points the next specs snap into.

## Amendments

Recorded after implementation, where the spec as written was ambiguous or has been
superseded by a later spec.

- **A reused consent link is rejected, not idempotent** (decided while building
  ticket #4). The API contract above originally read "idempotent for the same link;
  rejects reused/expired links", which contradicts itself: idempotency means a
  second submission silently succeeds, and rejection means it does not. Story 16 is
  the tie-breaker — a Guardian wants a used link to "fail safely rather than
  silently do nothing", so they know to request a fresh one. Submitting an
  already-used link therefore returns **409 Conflict**
  (`ConsentLinkAlreadyUsedException`), and an expired one fails the same way. The
  consent itself remains a single event: the minor's state flips once, so replaying
  the link can never grant consent twice.
- **The Mentor application carries a headline and bio** (decided while building
  spec 0002 ticket #12). Out of Scope above excludes "rich Mentor profile fields
  … beyond what the application/role model needs" — spec 0002 established the
  need, since it seeds a Mentor's draft profile from their application so an
  approved Mentor does not start from a blank page. Headline and bio are collected
  at application time; the remaining presentation fields (price, languages,
  employer, Meeting duration, Field(s)) stay out of the application and belong to
  the Mentor profile editor.
- **A Reviewer can lift a suspension** (decided while building spec 0002 ticket
  #13). Story 29 gave a Reviewer the power to suspend an approved Mentor but never
  the reverse, which left spec 0002's story 24 ("if my suspension is lifted I
  don't have to rebuild it") unreachable. Suspension is now two-way, and remains
  distinct from application status: a suspended Mentor is still APPROVED.
