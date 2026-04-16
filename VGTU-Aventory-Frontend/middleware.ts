import { NextRequest, NextResponse } from "next/server";
import { AUTH_TOKEN_COOKIE } from "@/features/auth/constants";

const PUBLIC_PATHS = ["/login", "/register"];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (
    pathname.startsWith("/_next") ||
    pathname.startsWith("/api") ||
    pathname === "/favicon.ico" ||
    PUBLIC_PATHS.some((path) => pathname === path || pathname.startsWith(`${path}/`))
  ) {
    return NextResponse.next();
  }

  const token = request.cookies.get(AUTH_TOKEN_COOKIE)?.value;

  if (!token) {
    const loginUrl = new URL("/login", request.url);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};