import { z } from "zod"
import { router, publicProcedure } from "../trpc"
import { authRouter } from "./auth"
import { userRouter } from "./user"
import { meetingRouter } from "./meeting"
import { mentorRouter } from "./mentor"
import { communityRouter } from "./community"

export const appRouter = router({
  auth: authRouter,
  user: userRouter,
  meeting: meetingRouter,
  mentor: mentorRouter,
  community: communityRouter,
  hello: publicProcedure.input(z.object({ text: z.string() })).query(({ input }) => {
    return {
      greeting: `Hello ${input.text}`,
    }
  }),
})

export type AppRouter = typeof appRouter
