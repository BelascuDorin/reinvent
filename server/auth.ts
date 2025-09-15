import type { NextAuthOptions } from "next-auth"
import CredentialsProvider from "next-auth/providers/credentials"
import { compare, hash } from "bcryptjs"

const users = new Map([
  [
    "mentor@test.com",
    {
      id: "1",
      email: "mentor@test.com",
      password: "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/VcSAg/9qm", // "password123"
      name: "Ana Popescu",
      role: "mentor",
    },
  ],
  [
    "mentee@test.com",
    {
      id: "2",
      email: "mentee@test.com",
      password: "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/VcSAg/9qm", // "password123"
      name: "Mihai Ionescu",
      role: "mentee",
    },
  ],
])

export const authOptions: NextAuthOptions = {
  secret: process.env.NEXTAUTH_SECRET || "fallback-secret-for-development",

  providers: [
    CredentialsProvider({
      name: "credentials",
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" },
        role: { label: "Role", type: "text" },
      },
      async authorize(credentials) {
        console.log("[v0] NextAuth authorize called with:", { email: credentials?.email, role: credentials?.role })

        if (!credentials?.email || !credentials?.password) {
          console.log("[v0] Missing credentials")
          return null
        }

        const user = users.get(credentials.email)

        if (!user) {
          console.log("[v0] User not found:", credentials.email)
          return null
        }

        const isPasswordValid = await compare(credentials.password, user.password)

        if (!isPasswordValid) {
          console.log("[v0] Invalid password for:", credentials.email)
          return null
        }

        console.log("[v0] User authenticated successfully:", user.email)
        return {
          id: user.id,
          email: user.email,
          role: user.role,
          name: user.name,
        }
      },
    }),
  ],
  session: {
    strategy: "jwt",
    maxAge: 30 * 24 * 60 * 60, // 30 days
  },
  callbacks: {
    jwt: ({ token, user }) => {
      console.log("[v0] JWT callback - token:", token, "user:", user)
      if (user) {
        token.role = user.role
      }
      return token
    },
    session: ({ session, token }) => {
      console.log("[v0] Session callback - session:", session, "token:", token)
      if (token) {
        session.user.id = token.sub!
        session.user.role = token.role as "mentor" | "mentee"
      }
      return session
    },
  },
  pages: {
    signIn: "/auth/signin",
  },
  debug: process.env.NODE_ENV === "development",
}

export async function registerUser(email: string, password: string, name: string, role: "mentor" | "mentee") {
  console.log("[v0] Registering new user:", { email, name, role })

  if (users.has(email)) {
    throw new Error("User already exists")
  }

  const hashedPassword = await hash(password, 12)
  const newUser = {
    id: Date.now().toString(),
    email,
    password: hashedPassword,
    name,
    role,
  }

  users.set(email, newUser)
  console.log("[v0] User registered successfully:", email)
  return { id: newUser.id, email: newUser.email, name: newUser.name, role: newUser.role }
}
