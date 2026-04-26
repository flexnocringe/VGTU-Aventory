"use client";

import { FormEvent, useEffect, useState } from "react";
import { getTopSellingProducts } from "@/features/analytics/services/getTopSellingProducts";
import type { TopSellingProduct } from "@/features/analytics/types/analytics";

function getTodayIsoDate(): string {
  return new Date().toISOString().split("T")[0];
}

export function TopSellingProducts() {
  const today = getTodayIsoDate();
  const startDateLaterThanEndDateErrorMessage = "Start date cannot be later than end date.";
  const endDateInFutureErrorMessage = "End date cannot be in the future.";

  const [startDate, setStartDate] = useState(today);
  const [endDate, setEndDate] = useState(today);
  const [topSellingProducts, setTopSellingProducts] = useState<TopSellingProduct[]>([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (startDate > endDate) {
      setError(startDateLaterThanEndDateErrorMessage);
      return;
    }

    if (endDate > today) {
      setError(endDateInFutureErrorMessage);
      return;
    }

    setError(null);
  }, [startDate, endDate, today]);

  async function handleLoadTopSellingProducts(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (startDate > endDate || endDate > today) {
      return;
    }

    setIsLoading(true);

    try {
      const data = await getTopSellingProducts({ startDate, endDate });
      setTopSellingProducts(data);
      setHasSearched(true);
      setError(null);
    } catch (requestError) {
      const message =
        requestError instanceof Error
          ? requestError.message
          : "Something went wrong while loading top selling products.";
      setError(message);
      setTopSellingProducts([]);
      setHasSearched(true);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)] sm:p-8">
      <h2 className="text-2xl font-semibold text-[#2d2418]">Top selling products</h2>
      <p className="mt-2 text-sm text-[#6a5841]">
        Select a date range and load products sorted by sales count.
      </p>

      <form className="mt-6 space-y-4" onSubmit={handleLoadTopSellingProducts}>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <label className="space-y-1">
            <span className="text-sm font-medium text-[#5b4a37]">Start date</span>
            <input
              id="top-selling-start-date"
              type="date"
              value={startDate}
              onChange={(event) => setStartDate(event.target.value)}
              className="w-full rounded-lg border border-[#e9dfcc] px-3 py-2 text-[#2d2418] outline-none ring-offset-2 transition focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
              required
            />
          </label>

          <label className="space-y-1">
            <span className="text-sm font-medium text-[#5b4a37]">End date</span>
            <input
              id="top-selling-end-date"
              type="date"
              max={today}
              value={endDate}
              onChange={(event) => setEndDate(event.target.value)}
              className="w-full rounded-lg border border-[#e9dfcc] px-3 py-2 text-[#2d2418] outline-none ring-offset-2 transition focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
              required
            />
          </label>
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="inline-flex items-center rounded-lg bg-[#f59e0b] px-4 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md disabled:cursor-not-allowed disabled:opacity-70"
        >
          {isLoading ? "Loading..." : "Load top selling products"}
        </button>
      </form>

      <div className="mt-6 rounded-xl bg-[#fff4e2] p-4">
        <p className="text-sm text-[#8a6b45]">Results</p>

        {error ? <p className="mt-2 text-sm text-red-600">{error}</p> : null}

        {!error && hasSearched && topSellingProducts.length === 0 ? (
          <p className="mt-2 text-sm text-[#6a5841]">No products sold in selected interval.</p>
        ) : null}

        {!error && topSellingProducts.length > 0 ? (
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
        ) : null}
      </div>
    </section>
  );
}
