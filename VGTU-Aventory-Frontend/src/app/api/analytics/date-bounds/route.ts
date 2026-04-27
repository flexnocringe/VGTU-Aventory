import { NextRequest, NextResponse } from "next/server";
import { createAuthHeaders } from "@/features/auth/session";

function getTodayIsoDate(): string {
  return new Date().toISOString().split("T")[0];
}

export async function GET(request: NextRequest) {
  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/firstSaleDate`;
  const cookieHeader = request.headers.get("cookie") ?? undefined;

  const backendResponse = await fetch(backendUrl, {
    method: "GET",
    headers: createAuthHeaders(undefined, cookieHeader),
    cache: "no-store",
  });

  if (!backendResponse.ok) {
    const errorText = await backendResponse.text();
    return NextResponse.json(
      { error: errorText || "Failed to fetch first sale date from backend." },
      { status: backendResponse.status },
    );
  }

  const data = (await backendResponse.json()) as unknown;
  const firstSaleDate =
    typeof data === "object" && data !== null && "firstSaleDate" in data
      ? String((data as { firstSaleDate: unknown }).firstSaleDate)
      : "";

  const first = new Date(firstSaleDate);
  if (!firstSaleDate || Number.isNaN(first.getTime())) {
    return NextResponse.json(
      { error: "Backend returned an invalid first sale date payload." },
      { status: 502 },
    );
  }

  return NextResponse.json({ firstSaleDate, today: getTodayIsoDate() });
}
