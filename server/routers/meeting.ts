import { z } from "zod"
import { router, protectedProcedure } from "../trpc"
import { TRPCError } from "@trpc/server"

// Simple in-memory store - replace with database in production
const meetings = new Map()
const bookings = new Map()

export const meetingRouter = router({
  // Mentor: Create available meeting slots
  createSlot: protectedProcedure
    .input(
      z.object({
        date: z.string(),
        startTime: z.string(),
        endTime: z.string(),
        price: z.number().min(0),
        title: z.string().min(1),
        description: z.string().optional(),
      }),
    )
    .mutation(({ input, ctx }) => {
      if (ctx.session.user.role !== "mentor") {
        throw new TRPCError({ code: "FORBIDDEN", message: "Only mentors can create meeting slots" })
      }

      const meetingId = Math.random().toString(36).substr(2, 9)
      const meeting = {
        id: meetingId,
        mentorId: ctx.session.user.id,
        mentorName: ctx.session.user.name,
        ...input,
        isBooked: false,
        createdAt: new Date(),
      }

      meetings.set(meetingId, meeting)

      return {
        success: true,
        meeting,
      }
    }),

  // Mentor: Get their meeting slots
  getMentorSlots: protectedProcedure.query(({ ctx }) => {
    if (ctx.session.user.role !== "mentor") {
      throw new TRPCError({ code: "FORBIDDEN" })
    }

    const mentorSlots = Array.from(meetings.values())
      .filter((meeting) => meeting.mentorId === ctx.session.user.id)
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())

    return mentorSlots
  }),

  // Mentee: Browse available meetings
  getAvailableSlots: protectedProcedure
    .input(
      z.object({
        mentorId: z.string().optional(),
        date: z.string().optional(),
      }),
    )
    .query(({ input }) => {
      let availableSlots = Array.from(meetings.values()).filter((meeting) => !meeting.isBooked)

      if (input.mentorId) {
        availableSlots = availableSlots.filter((meeting) => meeting.mentorId === input.mentorId)
      }

      if (input.date) {
        availableSlots = availableSlots.filter((meeting) => meeting.date === input.date)
      }

      return availableSlots.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
    }),

  // Mentee: Book a meeting
  bookMeeting: protectedProcedure
    .input(
      z.object({
        meetingId: z.string(),
        message: z.string().optional(),
      }),
    )
    .mutation(({ input, ctx }) => {
      if (ctx.session.user.role !== "mentee") {
        throw new TRPCError({ code: "FORBIDDEN", message: "Only mentees can book meetings" })
      }

      const meeting = meetings.get(input.meetingId)
      if (!meeting) {
        throw new TRPCError({ code: "NOT_FOUND", message: "Meeting not found" })
      }

      if (meeting.isBooked) {
        throw new TRPCError({ code: "CONFLICT", message: "Meeting is already booked" })
      }

      const bookingId = Math.random().toString(36).substr(2, 9)
      const booking = {
        id: bookingId,
        meetingId: input.meetingId,
        menteeId: ctx.session.user.id,
        menteeName: ctx.session.user.name,
        mentorId: meeting.mentorId,
        mentorName: meeting.mentorName,
        date: meeting.date,
        startTime: meeting.startTime,
        endTime: meeting.endTime,
        price: meeting.price,
        title: meeting.title,
        message: input.message,
        status: "pending", // pending, confirmed, completed, cancelled
        paymentStatus: meeting.price > 0 ? "pending" : "not_required",
        createdAt: new Date(),
      }

      bookings.set(bookingId, booking)

      // Mark meeting as booked
      meeting.isBooked = true
      meeting.bookingId = bookingId
      meetings.set(input.meetingId, meeting)

      return {
        success: true,
        booking,
      }
    }),

  // Get user's bookings (both mentor and mentee)
  getMyBookings: protectedProcedure.query(({ ctx }) => {
    const userBookings = Array.from(bookings.values())
      .filter((booking) => booking.menteeId === ctx.session.user.id || booking.mentorId === ctx.session.user.id)
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

    return userBookings
  }),

  // Update booking status
  updateBookingStatus: protectedProcedure
    .input(
      z.object({
        bookingId: z.string(),
        status: z.enum(["pending", "confirmed", "completed", "cancelled"]),
      }),
    )
    .mutation(({ input, ctx }) => {
      const booking = bookings.get(input.bookingId)
      if (!booking) {
        throw new TRPCError({ code: "NOT_FOUND", message: "Booking not found" })
      }

      // Only mentor can confirm/cancel, both can mark as completed
      if (input.status === "confirmed" || input.status === "cancelled") {
        if (booking.mentorId !== ctx.session.user.id) {
          throw new TRPCError({ code: "FORBIDDEN" })
        }
      }

      booking.status = input.status
      bookings.set(input.bookingId, booking)

      // If cancelled, make meeting available again
      if (input.status === "cancelled") {
        const meeting = meetings.get(booking.meetingId)
        if (meeting) {
          meeting.isBooked = false
          delete meeting.bookingId
          meetings.set(booking.meetingId, meeting)
        }
      }

      return {
        success: true,
        booking,
      }
    }),
})
