import type { TotalProfitRequest, TotalProfitResponse } from "@/features/analytics/types/analytics";
import { clearAuthToken } from "@/features/auth/session";

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

  if (response.status === 401 || response.status === 403) {
    clearAuthToken();
    if (typeof window !== "undefined" && window.location.pathname !== "/login") {
      window.location.assign("/login");
    }
    throw new Error("Session expired. Please sign in again.");
  }

  if (!response.ok) {
    throw new Error("Failed to fetch total profit.");
  }

  return (await response.json()) as TotalProfitResponse;
}
