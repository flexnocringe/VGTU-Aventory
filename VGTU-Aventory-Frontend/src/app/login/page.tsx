"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { loginUser } from "@/features/auth/services/loginUser";
import { setAuthSession } from "@/features/auth/session";

const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const clientError = useMemo(() => {
    if (!email || !password) {
      return null;
    }

    if (!emailRegex.test(email.trim())) {
      return "Email format is invalid";
    }

    if (!passwordRegex.test(password)) {
      return "Password must be at least 8 characters and contain at least one letter and one number";
    }

    return null;
  }, [email, password]);

  const onSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    if (!email || !password) {
      setError("Email and password are required");
      return;
    }

    if (clientError) {
      setError(clientError);
      return;
    }

    try {
      setLoading(true);
      const result = await loginUser({
        email: email.trim(),
        password,
      });

      setAuthSession(result.token, result.email);
      router.push("/");
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen px-6 py-12 sm:px-8">
      <div className="mx-auto w-full max-w-md rounded-2xl border border-[var(--border)] bg-[var(--card)] p-8 shadow-sm">
        <h1 className="text-3xl font-bold text-[var(--foreground)]">Sign In</h1>
        <p className="mt-2 text-sm text-[color-mix(in_oklab,var(--foreground)_70%,white)]">
          Use your registered email and password.
        </p>

        <form onSubmit={onSubmit} className="mt-6 space-y-4">
          <div>
            <label htmlFor="email" className="mb-1 block text-sm font-semibold text-[var(--foreground)]">
              Email
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="name@example.com"
              className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm outline-none transition focus:border-[var(--primary)] focus:ring-2 focus:ring-[var(--primary-soft)]"
            />
          </div>

          <div>
            <label htmlFor="password" className="mb-1 block text-sm font-semibold text-[var(--foreground)]">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Your password"
              className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm outline-none transition focus:border-[var(--primary)] focus:ring-2 focus:ring-[var(--primary-soft)]"
            />
          </div>

          {clientError && !error && (
            <p className="text-sm text-[var(--danger)]">{clientError}</p>
          )}

          {error && <p className="text-sm text-[var(--danger)]">{error}</p>}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-[var(--primary)] px-4 py-2 font-semibold text-white transition hover:bg-[var(--primary-hover)] disabled:cursor-not-allowed disabled:opacity-70"
          >
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </div>
    </main>
  );
}