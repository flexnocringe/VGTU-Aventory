"use client";

import { useEffect, useState } from "react";
import { getTopSellingProducts } from "@/features/analytics/services/getTopSellingProducts";
import type { TopSellingProduct } from "@/features/analytics/types/analytics";
import { useAnalyticsDates } from "@/features/analytics/context/AnalyticsDatesContext";

export function TopSellingProducts() {
  const { dateRange, error: dateError } = useAnalyticsDates();
  const [topSellingProducts, setTopSellingProducts] = useState<TopSellingProduct[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!dateRange.startDate || !dateRange.endDate) {
      return;
    }

    if (dateRange.startDate > dateRange.endDate) {
      return;
    }

    async function loadData() {
      setIsLoading(true);
      try {
        const data = await getTopSellingProducts({
          startDate: dateRange.startDate,
          endDate: dateRange.endDate,
        });
        setTopSellingProducts(data);
        setError(null);
      } catch (requestError) {
        const message =
          requestError instanceof Error
            ? requestError.message
            : "Something went wrong while loading top selling products.";
        setError(message);
        setTopSellingProducts([]);
      } finally {
        setIsLoading(false);
      }
    }

    loadData();
  }, [dateRange]);

  return (
    <section className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)] sm:p-8">
      <h2 className="text-2xl font-semibold text-[#2d2418]">Top selling products</h2>
      <p className="mt-2 text-sm text-[#6a5841]">Products sorted by sales count for selected date range.</p>

      <div className="mt-6 rounded-xl bg-[#fff4e2] p-4">
        <p className="text-sm text-[#8a6b45]">Results</p>

        {dateError ? (
          <p className="mt-2 text-sm text-red-600">{dateError}</p>
        ) : error ? (
          <p className="mt-2 text-sm text-red-600">{error}</p>
        ) : isLoading ? (
          <p className="mt-2 text-sm text-[#8a6b45]">Loading...</p>
        ) : topSellingProducts.length === 0 ? (
          <p className="mt-2 text-sm text-[#6a5841]">No products sold in selected interval.</p>
        ) : (
          <ul className="mt-3 space-y-2">
            {topSellingProducts.map((item) => (
              <li
                key={item.productId}
                className="flex items-center justify-between rounded-lg border border-[#f2e5d1] bg-white px-3 py-2"
              >
                <span className="text-sm font-medium text-[#2d2418]">{item.productName}</span>
                <span className="text-sm font-semibold text-[#9a6b2f]">{item.salesCount}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  );
}
