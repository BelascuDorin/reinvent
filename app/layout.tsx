import type React from "react";
import type { Metadata } from "next";
import { GeistSans } from "geist/font/sans";
import { GeistMono } from "geist/font/mono";
import { Analytics } from "@vercel/analytics/next";
import { SessionProvider } from "next-auth/react";
import { trpc } from "../lib/trpc";
import { Suspense } from "react";
import "./globals.css";

export const metadata: Metadata = {
  title: "MentorConnect - Connect with Romanian Professionals",
  description:
    "A platform connecting Romanian high school students with professional mentors for career guidance and insights.",
  generator: "v0.app",
};

function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`font-sans ${GeistSans.variable} ${GeistMono.variable}`}>
        <Suspense fallback={<div>Loading...</div>}>
          <SessionProvider>{children}</SessionProvider>
        </Suspense>
        <Analytics />
      </body>
    </html>
  );
}

export default trpc.withTRPC(RootLayout);
