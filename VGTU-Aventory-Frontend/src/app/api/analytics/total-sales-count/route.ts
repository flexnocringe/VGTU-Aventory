import { NextRequest, NextResponse } from "next/server";
import { createAuthHeaders } from "@/features/auth/session";

function getTodayIsoDate(): string {
  return new Date().toISOString().split("T")[0];
}

export async function GET(request: NextRequest) {
  const startDate = request.nextUrl.searchParams.get("startDate");
  const endDate = request.nextUrl.searchParams.get("endDate");

  if (!startDate || !endDate) {
    return NextResponse.json(
      { error: "Both startDate and endDate are required." },
      { status: 400 },
    );
  }

  const start = new Date(startDate);
  const end = new Date(endDate);

  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return NextResponse.json(
      { error: "Dates must be valid ISO date strings (YYYY-MM-DD)." },
      { status: 400 },
    );
  }

  if (start.getTime() > end.getTime()) {
    return NextResponse.json(
      { error: "startDate cannot be later than endDate." },
      { status: 400 },
    );
  }

  const today = getTodayIsoDate();
  if (endDate > today) {
    return NextResponse.json(
      { error: "endDate cannot be in the future." },
      { status: 400 },
    );
  }

  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/totalSalesCountInInterval/${encodeURIComponent(startDate)}/${encodeURIComponent(endDate)}`;
  const cookieHeader = request.headers.get("cookie") ?? undefined;

  const backendResponse = await fetch(backendUrl, {
    method: "GET",
    headers: createAuthHeaders(undefined, cookieHeader),
    cache: "no-store",
  });

  if (!backendResponse.ok) {
    const errorText = await backendResponse.text();
    return NextResponse.json(
      { error: errorText || "Failed to fetch total sales count from backend." },
      { status: backendResponse.status },
    );
  }

  const data = (await backendResponse.json()) as unknown;

  const totalSalesCount =
    typeof data === "number"
      ? data
      : typeof data === "object" && data !== null && "totalSalesCount" in data
        ? Number((data as { totalSalesCount: unknown }).totalSalesCount)
        : Number.NaN;

  if (!Number.isFinite(totalSalesCount) || !Number.isInteger(totalSalesCount) || totalSalesCount < 0) {
    return NextResponse.json(
      { error: "Backend returned an invalid total sales count payload." },
      { status: 502 },
    );
  }

  return NextResponse.json({ totalSalesCount });
}
