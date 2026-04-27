import { NextRequest, NextResponse } from "next/server";
import { createAuthHeaders } from "@/features/auth/session";

function getTodayIsoDate(): string {
  return new Date().toISOString().split("T")[0];
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ startDate: string; endDate: string }> },
) {
  const { startDate, endDate } = await params;

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

  let payload: { productIds?: unknown };
  try {
    payload = (await request.json()) as { productIds?: unknown };
  } catch {
    return NextResponse.json(
      { error: "Request body must be valid JSON." },
      { status: 400 },
    );
  }

  if (!Array.isArray(payload.productIds)) {
    return NextResponse.json(
      { error: "productIds must be an array of numbers." },
      { status: 400 },
    );
  }

  if (payload.productIds.length === 0) {
    return NextResponse.json(
      { error: "At least one product must be selected." },
      { status: 400 },
    );
  }

  if (payload.productIds.length > 5) {
    return NextResponse.json(
      { error: "Maximum 5 products can be compared." },
      { status: 400 },
    );
  }

  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/sales/sales-comparison/${encodeURIComponent(startDate)}/${encodeURIComponent(endDate)}`;
  const cookieHeader = request.headers.get("cookie") ?? undefined;

  const backendResponse = await fetch(backendUrl, {
    method: "POST",
    headers: {
      ...Object.fromEntries(createAuthHeaders(undefined, cookieHeader)),
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ productIds: payload.productIds }),
    cache: "no-store",
  });

  if (!backendResponse.ok) {
    const errorText = await backendResponse.text();
    return NextResponse.json(
      { error: errorText || "Failed to fetch product sales comparison from backend." },
      { status: backendResponse.status },
    );
  }

  const data = (await backendResponse.json()) as unknown;
  if (!Array.isArray(data)) {
    return NextResponse.json(
      { error: "Backend returned an invalid product sales comparison payload." },
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
      const totalSalesCount = Number((item as { totalSalesCount?: unknown }).totalSalesCount);

      if (!Number.isFinite(productId) || !Number.isInteger(productId)) {
        return null;
      }

      if (typeof productName !== "string") {
        return null;
      }

      if (!Number.isFinite(totalSalesCount) || !Number.isInteger(totalSalesCount) || totalSalesCount < 0) {
        return null;
      }

      return { productId, productName, totalSalesCount };
    })
    .filter((item): item is { productId: number; productName: string; totalSalesCount: number } => item !== null);

  return NextResponse.json(normalized);
}
