import { z } from "zod"
import { router, publicProcedure, protectedProcedure } from "../trpc"
import { TRPCError } from "@trpc/server"

// Simple in-memory stores - replace with database in production
const faqs = new Map()
const reviews = new Map()
const articles = new Map()
const meetingNotes = new Map()

// Sample FAQ data
const sampleFAQs = [
  {
    id: "faq1",
    question: "What skills are most important for your job?",
    category: "Skills & Requirements",
    isCommon: true,
    mentorId: null,
  },
  {
    id: "faq2",
    question: "What does a typical day look like in your role?",
    category: "Daily Work",
    isCommon: true,
    mentorId: null,
  },
  {
    id: "faq3",
    question: "How did you get started in this career?",
    category: "Career Path",
    isCommon: true,
    mentorId: null,
  },
  {
    id: "faq4",
    question: "What education or certifications do you recommend?",
    category: "Education",
    isCommon: true,
    mentorId: null,
  },
  {
    id: "faq5",
    question: "What are the biggest challenges in your field?",
    category: "Challenges",
    isCommon: true,
    mentorId: null,
  },
]

// Sample articles
const sampleArticles = [
  {
    id: "article1",
    title: "Breaking into Tech: A Romanian Student's Guide",
    excerpt: "Essential steps for Romanian high school students interested in technology careers.",
    content: "Technology is one of the fastest-growing industries in Romania...",
    authorId: "mentor1",
    authorName: "Ana Popescu",
    category: "Technology",
    publishedAt: new Date("2024-01-15"),
    readTime: 5,
    tags: ["Technology", "Career Advice", "Students"],
  },
  {
    id: "article2",
    title: "Marketing Careers in Romania: What You Need to Know",
    excerpt: "Insights into the marketing industry and how to build a successful career.",
    content: "The marketing landscape in Romania has evolved significantly...",
    authorId: "mentor2",
    authorName: "Mihai Ionescu",
    category: "Marketing",
    publishedAt: new Date("2024-01-10"),
    readTime: 7,
    tags: ["Marketing", "Digital Marketing", "Career Growth"],
  },
]

// Initialize sample data
sampleFAQs.forEach((faq) => faqs.set(faq.id, faq))
sampleArticles.forEach((article) => articles.set(article.id, article))

export const communityRouter = router({
  // FAQ Management
  getFAQs: publicProcedure
    .input(
      z.object({
        mentorId: z.string().optional(),
        category: z.string().optional(),
      }),
    )
    .query(({ input }) => {
      let filteredFAQs = Array.from(faqs.values())

      if (input.mentorId) {
        filteredFAQs = filteredFAQs.filter((faq) => faq.mentorId === input.mentorId || faq.isCommon)
      } else {
        filteredFAQs = filteredFAQs.filter((faq) => faq.isCommon)
      }

      if (input.category) {
        filteredFAQs = filteredFAQs.filter((faq) => faq.category === input.category)
      }

      // Group by category
      const grouped = filteredFAQs.reduce(
        (acc, faq) => {
          if (!acc[faq.category]) {
            acc[faq.category] = []
          }
          acc[faq.category].push(faq)
          return acc
        },
        {} as Record<string, any[]>,
      )

      return grouped
    }),

  addCustomFAQ: protectedProcedure
    .input(
      z.object({
        question: z.string().min(10),
        category: z.string().min(1),
      }),
    )
    .mutation(({ input, ctx }) => {
      if (ctx.session.user.role !== "mentor") {
        throw new TRPCError({ code: "FORBIDDEN" })
      }

      const faqId = Math.random().toString(36).substr(2, 9)
      const faq = {
        id: faqId,
        question: input.question,
        category: input.category,
        isCommon: false,
        mentorId: ctx.session.user.id,
        createdAt: new Date(),
      }

      faqs.set(faqId, faq)
      return { success: true, faq }
    }),

  // Reviews & Ratings
  addReview: protectedProcedure
    .input(
      z.object({
        mentorId: z.string(),
        bookingId: z.string(),
        rating: z.number().min(1).max(5),
        comment: z.string().optional(),
      }),
    )
    .mutation(({ input, ctx }) => {
      if (ctx.session.user.role !== "mentee") {
        throw new TRPCError({ code: "FORBIDDEN" })
      }

      const reviewId = Math.random().toString(36).substr(2, 9)
      const review = {
        id: reviewId,
        mentorId: input.mentorId,
        menteeId: ctx.session.user.id,
        menteeName: ctx.session.user.name,
        bookingId: input.bookingId,
        rating: input.rating,
        comment: input.comment,
        createdAt: new Date(),
      }

      reviews.set(reviewId, review)
      return { success: true, review }
    }),

  getMentorReviews: publicProcedure.input(z.object({ mentorId: z.string() })).query(({ input }) => {
    const mentorReviews = Array.from(reviews.values())
      .filter((review) => review.mentorId === input.mentorId)
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

    const avgRating =
      mentorReviews.length > 0
        ? mentorReviews.reduce((sum, review) => sum + review.rating, 0) / mentorReviews.length
        : 0

    return {
      reviews: mentorReviews,
      averageRating: Math.round(avgRating * 10) / 10,
      totalReviews: mentorReviews.length,
    }
  }),

  // Articles & Blog
  getArticles: publicProcedure
    .input(
      z.object({
        category: z.string().optional(),
        authorId: z.string().optional(),
        limit: z.number().default(10),
      }),
    )
    .query(({ input }) => {
      let filteredArticles = Array.from(articles.values())

      if (input.category) {
        filteredArticles = filteredArticles.filter((article) => article.category === input.category)
      }

      if (input.authorId) {
        filteredArticles = filteredArticles.filter((article) => article.authorId === input.authorId)
      }

      return filteredArticles
        .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
        .slice(0, input.limit)
    }),

  getArticleById: publicProcedure.input(z.object({ id: z.string() })).query(({ input }) => {
    return articles.get(input.id) || null
  }),

  createArticle: protectedProcedure
    .input(
      z.object({
        title: z.string().min(10),
        excerpt: z.string().min(20),
        content: z.string().min(100),
        category: z.string().min(1),
        tags: z.array(z.string()),
      }),
    )
    .mutation(({ input, ctx }) => {
      if (ctx.session.user.role !== "mentor") {
        throw new TRPCError({ code: "FORBIDDEN" })
      }

      const articleId = Math.random().toString(36).substr(2, 9)
      const article = {
        id: articleId,
        title: input.title,
        excerpt: input.excerpt,
        content: input.content,
        authorId: ctx.session.user.id,
        authorName: ctx.session.user.name,
        category: input.category,
        tags: input.tags,
        publishedAt: new Date(),
        readTime: Math.ceil(input.content.split(" ").length / 200), // Rough estimate
      }

      articles.set(articleId, article)
      return { success: true, article }
    }),

  // Meeting Notes
  addMeetingNotes: protectedProcedure
    .input(
      z.object({
        bookingId: z.string(),
        notes: z.string().min(10),
        isPrivate: z.boolean().default(false),
      }),
    )
    .mutation(({ input, ctx }) => {
      const noteId = Math.random().toString(36).substr(2, 9)
      const note = {
        id: noteId,
        bookingId: input.bookingId,
        authorId: ctx.session.user.id,
        authorName: ctx.session.user.name,
        authorRole: ctx.session.user.role,
        notes: input.notes,
        isPrivate: input.isPrivate,
        createdAt: new Date(),
      }

      meetingNotes.set(noteId, note)
      return { success: true, note }
    }),

  getMeetingNotes: protectedProcedure.input(z.object({ bookingId: z.string() })).query(({ input, ctx }) => {
    const notes = Array.from(meetingNotes.values())
      .filter(
        (note) => note.bookingId === input.bookingId && (!note.isPrivate || note.authorId === ctx.session.user.id),
      )
      .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())

    return notes
  }),
})
