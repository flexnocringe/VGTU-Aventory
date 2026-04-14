import { NextRequest, NextResponse } from "next/server";

export async function POST(request: NextRequest) {
  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/api/events/create`;

  try {
    const body = await request.json();
    const backendResponse = await fetch(backendUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json();
      return NextResponse.json(
        errorData || { error: "Failed to create event in backend." },
        { status: backendResponse.status },
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Create event fetch error:", error);
    return NextResponse.json(
      { error: "An error occurred while connecting to the backend." },
      { status: 500 },
    );
  }
}
