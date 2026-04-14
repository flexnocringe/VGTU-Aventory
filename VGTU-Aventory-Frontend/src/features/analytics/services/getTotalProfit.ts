import type { TotalProfitRequest, TotalProfitResponse } from "@/features/analytics/types/analytics";

export async function getTotalProfit(
  payload: TotalProfitRequest,
): Promise<TotalProfitResponse> {
  const searchParams = new URLSearchParams({
    startDate: payload.startDate,
    endDate: payload.endDate,
  });

  const response = await fetch(`/api/analytics/total-profit?${searchParams.toString()}`, {
    method: "GET",
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error("Failed to fetch total profit.");
  }

  return (await response.json()) as TotalProfitResponse;
}
