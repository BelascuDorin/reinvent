# Escrow-style payments via Stripe Connect

The Payer is charged at booking; the platform holds the funds and releases the
Payout (Meeting price minus Platform commission) to the Mentor only after the
Meeting is completed and a short dispute-hold window passes. Money movement,
Mentor KYC, and payouts run through Stripe Connect rather than being built
in-house. A Mentor must complete Stripe Connect onboarding before becoming
bookable.

## Considered Options

- **Charge at booking, pay Mentor immediately** — rejected; refunds would require clawing funds back from the Mentor.
- **Charge after the Meeting** — rejected; high non-payment / chargeback risk.
- **Build payouts/KYC in-house** — rejected; enormous scope for an early product.

## Consequences

- Hard dependency on Stripe Connect; its fees stack on top of the Platform commission.
- "Completed" is defined as the Slot end passing with no open Report; a Report pauses the Payout and alerts a Reviewer.
- Adds a payout-hold window concept to the Meeting lifecycle.
