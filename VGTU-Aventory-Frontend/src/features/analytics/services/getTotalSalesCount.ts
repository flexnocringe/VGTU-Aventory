import type { TotalProfitRequest, TotalSalesCountResponse } from "@/features/analytics/types/analytics";
import { clearAuthToken } from "@/features/auth/session";

export async function getTotalSalesCount(
  payload: TotalProfitRequest,
): Promise<TotalSalesCountResponse> {
  const searchParams = new URLSearchParams({
    startDate: payload.startDate,
    endDate: payload.endDate,
  });

  const response = await fetch(`/api/analytics/total-sales-count?${searchParams.toString()}`, {
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
    let errorMessage = "Failed to fetch total sales count.";
    try {
      const data = (await response.json()) as { error?: string };
      if (typeof data.error === "string" && data.error.length > 0) {
        errorMessage = data.error;
      }
    } catch {
      // Keep default message.
    }
    throw new Error(errorMessage);
  }

  return (await response.json()) as TotalSalesCountResponse;
}
