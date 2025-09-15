import { z } from "zod"
import { router, publicProcedure, protectedProcedure } from "../trpc"

// Simple in-memory store - replace with database in production
const mentors = new Map()

// Sample mentor data for demonstration
const sampleMentors = [
  {
    id: "mentor1",
    name: "Ana Popescu",
    jobTitle: "Senior Software Engineer",
    company: "Google Romania",
    bio: "Passionate about helping students discover the world of technology. 8+ years experience in full-stack development.",
    expertise: ["Software Development", "Web Development", "Career Guidance", "Technical Interviews"],
    industry: "Technology",
    experience: 8,
    rating: 4.9,
    reviewCount: 24,
    location: "Bucharest",
    languages: ["Romanian", "English"],
    meetingTypes: ["Career Guidance", "Technical Skills", "Interview Prep"],
    availability: "Weekday evenings",
    profileImage: "/professional-woman-software-engineer.png",
  },
  {
    id: "mentor2",
    name: "Mihai Ionescu",
    jobTitle: "Marketing Director",
    company: "eMAG",
    bio: "Marketing professional with 10+ years helping brands grow. Love mentoring students interested in marketing careers.",
    expertise: ["Digital Marketing", "Brand Strategy", "Social Media", "Career Development"],
    industry: "Marketing",
    experience: 10,
    rating: 4.8,
    reviewCount: 18,
    location: "Bucharest",
    languages: ["Romanian", "English"],
    meetingTypes: ["Career Guidance", "Marketing Strategy", "Personal Branding"],
    availability: "Flexible schedule",
    profileImage: "/professional-marketing-director.png",
  },
  {
    id: "mentor3",
    name: "Elena Radu",
    jobTitle: "Product Manager",
    company: "UiPath",
    bio: "Product management expert passionate about innovation and helping students understand tech product development.",
    expertise: ["Product Management", "UX Design", "Agile", "Leadership"],
    industry: "Technology",
    experience: 6,
    rating: 4.9,
    reviewCount: 31,
    location: "Cluj-Napoca",
    languages: ["Romanian", "English", "German"],
    meetingTypes: ["Product Strategy", "Career Guidance", "Leadership Skills"],
    availability: "Weekend mornings",
    profileImage: "/professional-woman-product-manager.png",
  },
]

// Initialize sample data
sampleMentors.forEach((mentor) => {
  mentors.set(mentor.id, mentor)
})

export const mentorRouter = router({
  // Get all mentors with filtering
  getMentors: publicProcedure
    .input(
      z.object({
        search: z.string().optional(),
        industry: z.string().optional(),
        expertise: z.string().optional(),
        location: z.string().optional(),
        minRating: z.number().optional(),
        sortBy: z.enum(["rating", "experience", "reviews"]).optional(),
      }),
    )
    .query(({ input }) => {
      let filteredMentors = Array.from(mentors.values())

      // Search filter
      if (input.search) {
        const searchLower = input.search.toLowerCase()
        filteredMentors = filteredMentors.filter(
          (mentor) =>
            mentor.name.toLowerCase().includes(searchLower) ||
            mentor.jobTitle.toLowerCase().includes(searchLower) ||
            mentor.company.toLowerCase().includes(searchLower) ||
            mentor.bio.toLowerCase().includes(searchLower) ||
            mentor.expertise.some((exp: string) => exp.toLowerCase().includes(searchLower)),
        )
      }

      // Industry filter
      if (input.industry) {
        filteredMentors = filteredMentors.filter((mentor) => mentor.industry === input.industry)
      }

      // Expertise filter
      if (input.expertise) {
        filteredMentors = filteredMentors.filter((mentor) => mentor.expertise.includes(input.expertise))
      }

      // Location filter
      if (input.location) {
        filteredMentors = filteredMentors.filter((mentor) => mentor.location === input.location)
      }

      // Rating filter
      if (input.minRating) {
        filteredMentors = filteredMentors.filter((mentor) => mentor.rating >= input.minRating)
      }

      // Sorting
      if (input.sortBy) {
        filteredMentors.sort((a, b) => {
          switch (input.sortBy) {
            case "rating":
              return b.rating - a.rating
            case "experience":
              return b.experience - a.experience
            case "reviews":
              return b.reviewCount - a.reviewCount
            default:
              return 0
          }
        })
      }

      return filteredMentors
    }),

  // Get mentor by ID
  getMentorById: publicProcedure.input(z.object({ id: z.string() })).query(({ input }) => {
    return mentors.get(input.id) || null
  }),

  // Get recommended mentors based on mentee interests
  getRecommendedMentors: protectedProcedure
    .input(
      z.object({
        interests: z.array(z.string()).optional(),
        limit: z.number().default(3),
      }),
    )
    .query(({ input }) => {
      let allMentors = Array.from(mentors.values())

      if (input.interests && input.interests.length > 0) {
        // Score mentors based on matching interests/expertise
        const scoredMentors = allMentors.map((mentor) => {
          const matchScore = input.interests.reduce((score, interest) => {
            const interestLower = interest.toLowerCase()
            const matches = mentor.expertise.filter(
              (exp: string) => exp.toLowerCase().includes(interestLower) || interestLower.includes(exp.toLowerCase()),
            ).length
            return score + matches
          }, 0)

          return { ...mentor, matchScore }
        })

        // Sort by match score and rating
        allMentors = scoredMentors
          .filter((mentor) => mentor.matchScore > 0)
          .sort((a, b) => {
            if (a.matchScore !== b.matchScore) {
              return b.matchScore - a.matchScore
            }
            return b.rating - a.rating
          })
      } else {
        // If no interests, sort by rating
        allMentors.sort((a, b) => b.rating - a.rating)
      }

      return allMentors.slice(0, input.limit)
    }),

  // Get filter options
  getFilterOptions: publicProcedure.query(() => {
    const allMentors = Array.from(mentors.values())

    const industries = [...new Set(allMentors.map((m) => m.industry))]
    const locations = [...new Set(allMentors.map((m) => m.location))]
    const allExpertise = allMentors.flatMap((m) => m.expertise)
    const expertise = [...new Set(allExpertise)]

    return {
      industries: industries.sort(),
      locations: locations.sort(),
      expertise: expertise.sort(),
    }
  }),
})
