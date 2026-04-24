import { createAuthHeaders } from "@/features/auth/session";
import { apiUrl } from "@/lib/api/url";

export async function logoutUser(): Promise<void> {
  const response = await fetch(apiUrl("/auth/logout"), {
    method: "POST",
    headers: createAuthHeaders(),
  });

  if (response.status === 401 || response.status === 403) {
    return;
  }

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Logout failed");
  }
}