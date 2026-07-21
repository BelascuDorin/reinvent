# Reinvent

A mentorship marketplace where teenagers exploring a career path can find and
meet people who already practice it, to help them decide whether that path fits.

## Language

**User**:
The single identity behind an account. A User can hold the Mentor role, the
Mentee role, or both. Everyone is a Mentee by default; becoming a Mentor is an
additional step. A User cannot book a Meeting with themselves.
_Avoid_: Account, member, profile

**Mentor**:
A role a User takes on to offer their time to be met with, setting their own
price and the timeslots they are available. A User only holds this role once a
Reviewer approves their Mentor application; a Mentor can be suspended later.

**Mentor application**:
A User's request to gain the Mentor role. It is manually reviewed before the
User can be booked, moving through: applied → under review → approved (or
rejected). This gate exists because Mentees are minors meeting adults.

**Reviewer**:
An internal, trusted actor (staff) who reviews Mentor applications and can
suspend Mentors. Not a Mentor or Mentee.
_Avoid_: Admin, moderator

**Guardian**:
The parent/guardian a minor Mentee names at signup. Not a User of the product;
they exist only as the party who gives Guardian consent and receives booking
notifications.
_Avoid_: Parent, custodian

**Guardian consent**:
A one-time, platform-level approval given by a Guardian via a link, confirming
both that they are the Mentee's guardian and that they consent to the Mentee
using Reinvent. A minor Mentee stays in "pending guardian consent" and cannot
book Meetings until it is given. Treated as a consent record, not proof of
identity. It is not re-requested per Meeting.

**Payer**:
The party charged for a Meeting. For a minor Mentee this is their Guardian
(whose payment method is captured); for an adult Mentee it is the Mentee
themselves. The Mentee is never charged when they are a minor.

**Platform commission**:
The percentage of a Meeting's price that Reinvent keeps; the rest is the
Mentor's earnings. Taken as a Stripe Connect application fee. The exact rate is
a configurable business setting.
_Avoid_: Fee, cut, take rate

**Payout**:
The transfer of a Meeting's price (minus Platform commission) to the Mentor,
released only after the Meeting is completed and a short dispute-hold window
passes.

**No-show**:
A terminal Meeting outcome where a party did not attend. A Mentor no-show fully
refunds the Payer; a Mentee no-show still pays the Mentor.

**Review**:
A Mentee's (or their Guardian's) feedback on a completed Meeting: a 1–5 rating
plus optional text, shown on the Mentor profile. One per completed Meeting.
_Avoid_: Rating (the score alone), feedback, testimonial

**Report**:
A safety flag any party can raise against a Meeting or Mentor. It opens a
dispute that pauses the Payout, alerts a Reviewer, and can lead to Mentor
suspension.
_Avoid_: Flag, complaint, dispute

**Mentee**:
A role a User takes on to discover Mentors and book Meetings with them — the
target user is a teenager exploring a possible career path.
_Avoid_: Student, mentoree

**Meeting**:
The core bookable unit. A single, standalone, video-only session between one
Mentee and one Mentor, created when a Mentee books a Slot. Its lifecycle is
booked → completed / cancelled / no_show. Rebooking is a new, independent
Meeting; there is no ongoing "mentorship relationship" entity.
_Avoid_: Session, appointment, mentorship

**Meeting link**:
The video venue for a Meeting: a URL produced by a swappable VideoProvider.
Jitsi is the launch provider (no accounts, join in-browser); Google Meet is a
possible later swap. The domain never depends on a specific provider.
_Avoid_: Room, call URL, Zoom link

**Slot**:
A specific, discrete window of time a Mentor opens for booking (e.g. "Tue 20 Jul
16:00–16:30 UTC"). Its length is the Mentor's fixed meeting duration. Booking a
Slot atomically consumes it and creates a Meeting. All times are stored in UTC
and rendered in the viewer's local zone.
_Avoid_: Timeslot, availability, opening

**Meeting duration**:
A single fixed length (e.g. 30 or 60 min) chosen by a Mentor that applies to all
of their Slots.

**Field**:
A career area a Mentor practises and a Mentee explores (e.g. "Software
Engineering", "Medicine"). Drawn from a curated, controlled list — not free
text — so it is the primary axis for discovery.
_Avoid_: Category, tag, industry, specialty

**Mentor profile**:
The public presentation of a Mentor: display name, current role/title, Field(s),
bio, experience, languages, price, Meeting duration, open Slots, and rating
summary. Employer is optional.

**Meeting brief**:
A short, structured note a Mentee writes at booking describing what they want to
discuss. It is the only Mentee-authored text a Mentor sees before the Meeting —
there is no free-form messaging between Mentor and Mentee.
_Avoid_: Message, chat, note
