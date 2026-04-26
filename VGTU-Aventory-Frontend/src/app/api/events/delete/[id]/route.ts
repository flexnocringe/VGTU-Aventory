import { NextRequest, NextResponse } from "next/server";
import { createAuthHeaders } from "@/features/auth/session";

export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;
  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/api/events/delete/${id}`;
  const cookieHeader = request.headers.get("cookie") ?? undefined;

  try {
    const backendResponse = await fetch(backendUrl, {
      method: "DELETE",
      headers: createAuthHeaders(undefined, cookieHeader),
    });

    if (!backendResponse.ok) {
      let errorData;
      try {
        errorData = await backendResponse.json();
      } catch (e) {
        errorData = { error: "Failed to delete event in backend." };
      }
      return NextResponse.json(
        errorData,
        { status: backendResponse.status },
      );
    }

    const data = await backendResponse.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Delete event fetch error:", error);
    return NextResponse.json(
      { error: "An error occurred while connecting to the backend." },
      { status: 500 },
    );
  }
}
