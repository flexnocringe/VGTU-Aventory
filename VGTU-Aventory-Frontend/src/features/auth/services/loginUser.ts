import { apiUrl } from "@/lib/api/url";
import { LoginRequest, LoginResponse } from "@/features/auth/types/auth";

export async function loginUser(payload: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(apiUrl("/auth/login"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Login failed");
  }

  return (await response.json()) as LoginResponse;
}