import type { TopSellingProduct, TopSellingProductsRequest } from "@/features/analytics/types/analytics";
import { clearAuthToken } from "@/features/auth/session";

export async function getTopSellingProducts(
  payload: TopSellingProductsRequest,
): Promise<TopSellingProduct[]> {
  const response = await fetch(`/api/analytics/top-selling/${payload.startDate}/${payload.endDate}`, {
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
    let errorMessage = "Failed to fetch top selling products.";

    try {
      const data = (await response.json()) as { error?: string };
      if (typeof data.error === "string" && data.error.length > 0) {
        errorMessage = data.error;
      }
    } catch {
      // keep default error message
    }

    throw new Error(errorMessage);
  }

  return (await response.json()) as TopSellingProduct[];
}
