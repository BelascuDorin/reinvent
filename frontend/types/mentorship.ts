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
  priceAmount: number | null
  priceCurrency: string | null
  meetingDurationMinutes: number | null
  fieldSlugs: string[]
  languages: string[]
  ratingSummary: RatingSummary | null
}

/**
 * A Mentor's whole public presentation, as a visitor reads it and as the Mentor previews
 * it. `employer` is absent from the response when the Mentor left it blank, so it is
 * optional here rather than nullable.
 */
export interface PublicMentorProfile {
  mentorUserId: string
  displayName: string | null
  roleTitle: string | null
  bio: string | null
  experience: string | null
  employer?: string
  priceAmount: number | null
  priceCurrency: string | null
  meetingDurationMinutes: number | null
  fieldSlugs: string[]
  languages: string[]
  ratingSummary: RatingSummary | null
}

/**
 * A Mentor application and where it stands. `suspended` is separate from `status`: a
 * suspended Mentor is still APPROVED. `bookable` additionally requires payment
 * onboarding, which discovery deliberately ignores.
 */
export interface MentorApplication {
  id: string
  applicantUserId: string
  status: 'APPLIED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED'
  /** What the applicant wrote; seeds their draft Mentor profile on approval. */
  headline: string | null
  bio: string | null
  rejectionReason: string | null
  suspended: boolean
  bookable: boolean
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
