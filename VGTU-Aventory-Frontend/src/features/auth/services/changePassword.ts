import { apiUrl } from "@/lib/api/url";
import { ChangePasswordRequest } from "@/features/auth/types/auth";
import { fetchWithSession } from "@/features/auth/services/fetchWithSession";

export async function changePassword(payload: ChangePasswordRequest): Promise<void> {
  const response = await fetchWithSession(apiUrl("/auth/change-password"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Password change failed");
  }
}