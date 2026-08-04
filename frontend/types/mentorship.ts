// The mentorship shapes the backend returns, shared by every page that reads them so
// the contract is written down once. Names follow CONTEXT.md: Field, Mentor, Mentor
// profile, Meeting duration.

/** A curated career area a Mentor practises and a visitor browses by. */
export interface Field {
  slug: string
  displayName: string
}

/** A Mentor's rating, once the Reviews slice exists — null on every summary today. */
export interface RatingSummary {
  averageRating: number
  reviewCount: number
}

/**
 * How a Mentor appears in public discovery. Only discoverable Mentors are listed, so
 * there is no completeness flag to check: being here is what discoverable means.
 */
export interface MentorSummary {
  mentorUserId: string
  displayName: string | null
  roleTitle: string | null
  employer: string | null
  priceAmount: number | null
  priceCurrency: string | null
  meetingDurationMinutes: number | null
  fieldSlugs: string[]
  languages: string[]
  ratingSummary: RatingSummary | null
}

/** The signed-in Mentor's own view of their profile, including where they stand. */
export interface MentorProfile {
  mentorUserId: string
  displayName: string | null
  roleTitle: string | null
  bio: string | null
  experience: string | null
  employer: string | null
  priceAmount: number | null
  priceCurrency: string | null
  meetingDurationMinutes: number | null
  fieldSlugs: string[]
  languages: string[]
  complete: boolean
  discoverable: boolean
}
