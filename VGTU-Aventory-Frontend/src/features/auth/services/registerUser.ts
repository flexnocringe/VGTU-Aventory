import { apiUrl } from "@/lib/api/url";
import { RegisterRequest, RegisterResponse } from "@/features/auth/types/auth";

export async function registerUser(payload: RegisterRequest): Promise<RegisterResponse> {
  const response = await fetch(apiUrl("/auth/register"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Registration failed");
  }

  return (await response.json()) as RegisterResponse;
}
