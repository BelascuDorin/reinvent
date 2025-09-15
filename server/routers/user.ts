import { z } from "zod"
import { router, protectedProcedure } from "../trpc"

export const userRouter = router({
  getProfile: protectedProcedure.query(({ ctx }) => {
    return {
      id: ctx.session.user.id,
      email: ctx.session.user.email,
      name: ctx.session.user.name,
      role: ctx.session.user.role,
    }
  }),

  updateProfile: protectedProcedure
    .input(
      z.object({
        name: z.string().optional(),
        bio: z.string().optional(),
        expertise: z.array(z.string()).optional(),
        meetingFee: z.number().optional(),
      }),
    )
    .mutation(({ input, ctx }) => {
      // Update user profile logic here
      return {
        success: true,
        message: "Profile updated successfully",
      }
    }),
})
