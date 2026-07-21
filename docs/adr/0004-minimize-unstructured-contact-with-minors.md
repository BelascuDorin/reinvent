# Minimize unstructured contact with minors: video-only, no free-form messaging

To reduce safeguarding risk for minor Mentees, all Meetings are video-only (no
in-person option at launch) and there is no free-form Mentor↔Mentee messaging.
The only Mentee-authored text a Mentor sees before a Meeting is a structured
Meeting brief written at booking. Video happens over a swappable VideoProvider
(Jitsi at launch), never a provider the domain depends on.

## Considered Options

- **Allow in-person meetings** — deferred; physical meetings between adults and minors are a serious escalation needing dedicated safety design.
- **Free-form pre-meeting chat** — rejected as a grooming vector; replaced by the structured Meeting brief.
- **Commit to Google Meet directly** — rejected for launch due to OAuth/account and guest-admission friction for minors; kept as a later swap behind the VideoProvider interface.

## Consequences

- Excludes users who would value meeting locally — a deliberate deferral, not an oversight.
- Contact surface is limited to: booking, the Meeting brief, and the video Meeting.
