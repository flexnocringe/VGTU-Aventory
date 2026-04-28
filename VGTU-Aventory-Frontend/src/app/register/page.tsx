"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { registerUser } from "@/features/auth/services/registerUser";
import { loginUser } from "@/features/auth/services/loginUser";
import { setAuthSession } from "@/features/auth/session";

const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

export default function RegisterPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

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
    setSuccessMessage(null);

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
      await registerUser({
        email: email.trim(),
        password,
      });

      const loginResult = await loginUser({
        email: email.trim(),
        password,
      });

      setAuthSession(loginResult.token, loginResult.email);
      router.push("/");
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen px-6 py-12 sm:px-8">
      <div className="mx-auto w-full max-w-md rounded-2xl border border-[var(--border)] bg-[var(--card)] p-8 shadow-sm">
        <h1 className="text-3xl font-bold text-[var(--foreground)]">Create Account</h1>
        <p className="mt-2 text-sm text-[color-mix(in_oklab,var(--foreground)_70%,white)]">
          Register with your email and password.
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
              placeholder="At least 8 chars, letters + numbers"
              className="w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm outline-none transition focus:border-[var(--primary)] focus:ring-2 focus:ring-[var(--primary-soft)]"
            />
          </div>

          {clientError && !error && (
            <p className="text-sm text-[var(--danger)]">{clientError}</p>
          )}

          {error && <p className="text-sm text-[var(--danger)]">{error}</p>}
          {successMessage && <p className="text-sm text-[var(--success)]">{successMessage}</p>}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-[var(--primary)] px-4 py-2 font-semibold text-white transition hover:bg-[var(--primary-hover)] disabled:cursor-not-allowed disabled:opacity-70"
          >
            {loading ? "Creating..." : "Create account"}
          </button>

          <div className="pt-2 text-center text-sm text-[color-mix(in_oklab,var(--foreground)_70%,white)]">
            Already have an account?{" "}
            <Link
              href="/login"
              className="font-semibold text-[var(--primary)] underline-offset-2 transition hover:text-[var(--primary-hover)] hover:underline"
            >
              Sign in
            </Link>
          </div>
        </form>
      </div>
    </main>
  );
}
