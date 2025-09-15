declare module "next-auth" {
  interface Session {
    user: {
      id: string
      email: string
      name: string
      role: "mentor" | "mentee"
    }
  }

  interface User {
    role: "mentor" | "mentee"
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    role: "mentor" | "mentee"
  }
}
