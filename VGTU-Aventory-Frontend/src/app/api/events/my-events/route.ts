import { NextResponse } from "next/server";

export async function GET() {
  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/api/events/my-events`;

  try {
    const backendResponse = await fetch(backendUrl, {
      method: "GET",
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
