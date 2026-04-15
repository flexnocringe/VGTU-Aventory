import { clearAuthToken, createAuthHeaders } from "@/features/auth/session";

const SESSION_EXPIRED_MESSAGE = "Session expired. Please sign in again.";

function redirectToLoginIfNeeded(): void {
  if (typeof window === "undefined") {
    return;
  }

  if (window.location.pathname !== "/login") {
    window.location.assign("/login");
  }
}

export async function fetchWithSession(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
  const response = await fetch(input, {
    ...init,
    headers: createAuthHeaders(init?.headers),
  });

  if (response.status === 401 || response.status === 403) {
    clearAuthToken();
    redirectToLoginIfNeeded();
    throw new Error(SESSION_EXPIRED_MESSAGE);
  }

  return response;
}