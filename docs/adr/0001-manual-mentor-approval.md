# Manual mentor approval before a Mentor is bookable

Because Mentees are minors meeting adults, a User who applies for the Mentor role
is not bookable until a human Reviewer approves their Mentor application
(applied → under review → approved/rejected), rather than going live self-serve.
We accept slow, manual onboarding as the price of vetting strangers before they
can meet teenagers.

## Considered Options

- **Self-serve** — instant, but an unvetted stranger is immediately bookable by a minor. Rejected as an unacceptable safeguarding risk.
- **Lightweight automated verification** — deferred; can be layered on later without changing the application state model.
- **Manual approval** — chosen for launch while Mentor volume is low.

## Consequences

- Introduces a Reviewer actor and an operational review burden.
- A Mentor is bookable only when *both* Reviewer-approved and Stripe Connect onboarded (see ADR-0003).
