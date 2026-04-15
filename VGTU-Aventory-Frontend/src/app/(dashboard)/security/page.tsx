"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { changePassword } from "@/features/auth/services/changePassword";
import { clearAuthToken } from "@/features/auth/session";

const PASSWORD_HELP = "Use at least 8 characters with one letter and one number.";

export default function SecurityPage() {
  const router = useRouter();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSuccess(null);

    if (!currentPassword || !newPassword || !confirmPassword) {
      setError("All password fields are required");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("New password and confirmation do not match");
      return;
    }

    try {
      setLoading(true);
      await changePassword({
        currentPassword,
        newPassword,
      });

      clearAuthToken();
      setSuccess("Password updated. Please sign in again.");
      router.push("/login");
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Password change failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="mx-auto max-w-2xl">
      <div className="rounded-3xl border border-[#f0dfc5] bg-[linear-gradient(180deg,#fffaf2_0%,#fff_100%)] p-6 shadow-sm md:p-8">
        <div className="max-w-xl">
          <p className="text-sm font-semibold uppercase tracking-[0.28em] text-[#9a6b2f]">Security</p>
          <h1 className="mt-2 text-3xl font-semibold text-[#2d2418]">Change password</h1>
          <p className="mt-3 text-sm leading-6 text-[#6b563f]">Update your password and the current session will be revoked immediately after the change.</p>
        </div>

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          <div>
            <label htmlFor="currentPassword" className="mb-1 block text-sm font-semibold text-[#2d2418]">Current password</label>
            <input
              id="currentPassword"
              type="password"
              value={currentPassword}
              onChange={(event) => setCurrentPassword(event.target.value)}
              className="w-full rounded-xl border border-[#e9d7bb] bg-white px-4 py-3 text-sm text-[#2d2418] outline-none transition focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/15"
            />
          </div>

          <div>
            <label htmlFor="newPassword" className="mb-1 block text-sm font-semibold text-[#2d2418]">New password</label>
            <input
              id="newPassword"
              type="password"
              value={newPassword}
              onChange={(event) => setNewPassword(event.target.value)}
              className="w-full rounded-xl border border-[#e9d7bb] bg-white px-4 py-3 text-sm text-[#2d2418] outline-none transition focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/15"
            />
            <p className="mt-2 text-xs text-[#8a6b45]">{PASSWORD_HELP}</p>
          </div>

          <div>
            <label htmlFor="confirmPassword" className="mb-1 block text-sm font-semibold text-[#2d2418]">Confirm new password</label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              className="w-full rounded-xl border border-[#e9d7bb] bg-white px-4 py-3 text-sm text-[#2d2418] outline-none transition focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/15"
            />
          </div>

          {error && <p className="rounded-xl border border-[#f1c2b0] bg-[#fff6f2] px-4 py-3 text-sm text-[#8f3d2d]">{error}</p>}
          {success && <p className="rounded-xl border border-[#c7e5c2] bg-[#f4fbf2] px-4 py-3 text-sm text-[#2f6a35]">{success}</p>}

          <button
            type="submit"
            disabled={loading}
            className="inline-flex items-center justify-center rounded-xl bg-[#f59e0b] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#d97706] disabled:cursor-not-allowed disabled:opacity-70"
          >
            {loading ? "Updating password..." : "Update password"}
          </button>
        </form>
      </div>
    </section>
  );
}