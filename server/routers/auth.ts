import { z } from "zod"
import { router, publicProcedure } from "../trpc"
import { hash } from "bcryptjs"

// Simple in-memory store - replace with database in production
const users = new Map()

export const authRouter = router({
  register: publicProcedure
    .input(
      z.object({
        email: z.string().email(),
        password: z.string().min(6),
        name: z.string().min(2),
        role: z.enum(["mentor", "mentee"]),
      }),
    )
    .mutation(async ({ input }) => {
      const { email, password, name, role } = input

      if (users.has(email)) {
        throw new Error("User already exists")
      }

      const hashedPassword = await hash(password, 12)
      const user = {
        id: Math.random().toString(36).substr(2, 9),
        email,
        password: hashedPassword,
        name,
        role,
        createdAt: new Date(),
      }

      users.set(email, user)

      return {
        success: true,
        user: {
          id: user.id,
          email: user.email,
          name: user.name,
          role: user.role,
        },
      }
    }),
})
