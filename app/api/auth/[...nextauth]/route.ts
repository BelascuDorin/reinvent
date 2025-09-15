import NextAuth from "next-auth"
import { authOptions } from "../../../../server/auth"

console.log("[v0] NextAuth API route initialized")

const handler = NextAuth(authOptions)

export { handler as GET, handler as POST }
