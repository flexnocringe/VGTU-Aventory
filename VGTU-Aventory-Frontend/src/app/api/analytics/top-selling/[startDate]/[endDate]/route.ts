import { NextRequest, NextResponse } from "next/server";
import { createAuthHeaders } from "@/features/auth/session";

function getTodayIsoDate(): string {
  return new Date().toISOString().split("T")[0];
}

function parseDate(value: string): Date {
  return new Date(value);
}

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ startDate: string; endDate: string }> },
) {
  const { startDate, endDate } = await params;

  const start = parseDate(startDate);
  const end = parseDate(endDate);

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
  const backendUrl = `${backendBaseUrl}/sales/top-selling/${encodeURIComponent(startDate)}/${encodeURIComponent(endDate)}`;
  const cookieHeader = request.headers.get("cookie") ?? undefined;

  const backendResponse = await fetch(backendUrl, {
    method: "GET",
    headers: createAuthHeaders(undefined, cookieHeader),
    cache: "no-store",
  });

  if (!backendResponse.ok) {
    const errorText = await backendResponse.text();
    return NextResponse.json(
      { error: errorText || "Failed to fetch top selling products from backend." },
      { status: backendResponse.status },
    );
  }

  const data = (await backendResponse.json()) as unknown;
  if (!Array.isArray(data)) {
    return NextResponse.json(
      { error: "Backend returned an invalid top selling products payload." },
      { status: 502 },
    );
  }

  const normalized = data
    .map((item) => {
      if (typeof item !== "object" || item === null) {
        return null;
      }

      const productId = Number((item as { productId?: unknown }).productId);
      const productName = (item as { productName?: unknown }).productName;
      const salesCount = Number((item as { salesCount?: unknown }).salesCount);

      if (!Number.isFinite(productId) || !Number.isInteger(productId)) {
        return null;
      }

      if (typeof productName !== "string") {
        return null;
      }

      if (!Number.isFinite(salesCount) || !Number.isInteger(salesCount) || salesCount <= 0) {
        return null;
      }

      return { productId, productName, salesCount };
    })
    .filter((item): item is { productId: number; productName: string; salesCount: number } => item !== null);

  return NextResponse.json(normalized);
}
