import { AUTH_TOKEN_COOKIE } from "@/features/auth/constants";

function readCookieValue(cookieSource: string | undefined, cookieName: string): string | undefined {
  if (!cookieSource) {
    return undefined;
  }

  const cookies = cookieSource.split(";");
  const match = cookies.find((cookie) => cookie.trim().startsWith(`${cookieName}=`));

  if (!match) {
    return undefined;
  }

  return decodeURIComponent(match.trim().slice(cookieName.length + 1));
}

export function getAuthToken(cookieSource?: string): string | undefined {
  if (cookieSource) {
    return readCookieValue(cookieSource, AUTH_TOKEN_COOKIE);
  }

  if (typeof document === "undefined") {
    return undefined;
  }

  return readCookieValue(document.cookie, AUTH_TOKEN_COOKIE);
}

export function createAuthHeaders(initHeaders?: HeadersInit, cookieSource?: string): Headers {
  const headers = new Headers(initHeaders);
  const token = getAuthToken(cookieSource);

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  return headers;
}

export function setAuthToken(token: string): void {
  if (typeof document === "undefined") {
    return;
  }

  const maxAgeSeconds = 60 * 60 * 8;
  document.cookie = `${AUTH_TOKEN_COOKIE}=${encodeURIComponent(token)}; path=/; max-age=${maxAgeSeconds}; samesite=lax`;
}

export function clearAuthToken(): void {
  if (typeof document === "undefined") {
    return;
  }

  document.cookie = `${AUTH_TOKEN_COOKIE}=; path=/; max-age=0; samesite=lax`;
}