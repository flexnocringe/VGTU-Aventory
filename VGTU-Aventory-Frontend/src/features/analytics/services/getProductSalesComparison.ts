import type { ProductSalesComparison } from "@/features/analytics/types/analytics";
import { clearAuthToken } from "@/features/auth/session";

export type ProductSalesComparisonRequest = {
  startDate: string;
  endDate: string;
  productIds: number[];
};

export async function getProductSalesComparison(
  payload: ProductSalesComparisonRequest,
): Promise<ProductSalesComparison[]> {
  const response = await fetch(`/api/analytics/product-sales/${payload.startDate}/${payload.endDate}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ productIds: payload.productIds }),
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
    let errorMessage = "Failed to fetch product sales comparison.";

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

  return (await response.json()) as ProductSalesComparison[];
}
