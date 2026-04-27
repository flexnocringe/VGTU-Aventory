import { NextRequest, NextResponse } from "next/server";
import { createAuthHeaders } from "@/features/auth/session";

export async function GET(request: NextRequest) {
  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/api/events/my-events`;
  const cookieHeader = request.headers.get("cookie") ?? undefined;

  try {
    const backendResponse = await fetch(backendUrl, {
      method: "GET",
      headers: createAuthHeaders(undefined, cookieHeader),
      cache: "no-store",
    });

    if (!backendResponse.ok) {
      return NextResponse.json(
        { error: "Failed to fetch events from backend." },
        { status: backendResponse.status },
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Fetch error:", error);
    return NextResponse.json(
      { error: "An error occurred while connecting to the backend." },
      { status: 500 },
    );
  }
}
