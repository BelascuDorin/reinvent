"use client";

import type React from "react";
import { SessionProvider } from "next-auth/react";
import { trpc } from "../lib/trpc";
import { Suspense } from "react";

interface ProvidersProps {
  children: React.ReactNode;
}

export function Providers({ children }: ProvidersProps) {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <SessionProvider>{children}</SessionProvider>
    </Suspense>
  );
}

export default trpc.withTRPC(Providers);
