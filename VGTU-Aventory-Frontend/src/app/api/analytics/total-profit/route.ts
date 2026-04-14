import { NextRequest, NextResponse } from "next/server";

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

  const backendBaseUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const backendUrl = `${backendBaseUrl}/totalProfitInInterval/${encodeURIComponent(startDate)}/${encodeURIComponent(endDate)}`;

  const backendResponse = await fetch(backendUrl, {
    method: "GET",
    cache: "no-store",
  });

  if (!backendResponse.ok) {
    return NextResponse.json(
      { error: "Failed to fetch total profit from backend." },
      { status: backendResponse.status },
    );
  }

  const data = (await backendResponse.json()) as unknown;

  const totalProfit =
    typeof data === "number"
      ? data
      : typeof data === "object" && data !== null && "totalProfit" in data
        ? Number((data as { totalProfit: unknown }).totalProfit)
        : Number.NaN;

  if (Number.isNaN(totalProfit)) {
    return NextResponse.json(
      { error: "Backend returned an invalid total profit payload." },
      { status: 502 },
    );
  }

  return NextResponse.json({ totalProfit });
}
