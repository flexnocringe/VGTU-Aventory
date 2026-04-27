import { NextRequest, NextResponse } from "next/server";
import { createAuthHeaders } from "@/features/auth/session";

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;
  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/api/events/edit/${id}`;
  const cookieHeader = request.headers.get("cookie") ?? undefined;

  try {
    const body = await request.json();
    const backendResponse = await fetch(backendUrl, {
      method: "PUT",
      headers: createAuthHeaders(
        { "Content-Type": "application/json" },
        cookieHeader
      ),
      body: JSON.stringify(body),
    });

    if (!backendResponse.ok) {
      let errorData;
      try {
        errorData = await backendResponse.json();
      } catch (e) {
        errorData = { error: "Failed to update event in backend." };
      }
      return NextResponse.json(
        errorData,
        { status: backendResponse.status },
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Update event fetch error:", error);
    return NextResponse.json(
      { error: "An error occurred while connecting to the backend." },
      { status: 500 },
    );
  }
}
