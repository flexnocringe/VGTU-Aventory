"use client";

import { useEffect, useState } from "react";
import { getTotalProfit } from "@/features/analytics/services/getTotalProfit";
import { useAnalyticsDates } from "@/features/analytics/context/AnalyticsDatesContext";

export function AnalyticsProfitCalculator() {
  const { dateRange, error: dateError } = useAnalyticsDates();
  const [totalProfit, setTotalProfit] = useState<number | null>(null);
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
        const data = await getTotalProfit({
          startDate: dateRange.startDate,
          endDate: dateRange.endDate,
        });
        setTotalProfit(data.totalProfit);
        setError(null);
      } catch (requestError) {
        const message =
          requestError instanceof Error
            ? requestError.message
            : "Something went wrong while calculating total profit.";
        setError(message);
        setTotalProfit(null);
      } finally {
        setIsLoading(false);
      }
    }

    loadData();
  }, [dateRange]);

  return (
    <section className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)] sm:p-8">
      <h2 className="text-2xl font-semibold text-[#2d2418]">Total Profit</h2>
      <p className="mt-2 text-sm text-[#6a5841]">Total profit for selected date range.</p>

      <div className="mt-6 rounded-xl bg-[#fff4e2] p-4">
        <p className="text-sm text-[#8a6b45]">Total profit</p>

        {dateError ? (
          <p className="mt-2 text-sm text-red-600">{dateError}</p>
        ) : error ? (
          <p className="mt-2 text-sm text-red-600">{error}</p>
        ) : isLoading ? (
          <p className="mt-2 text-sm text-[#8a6b45]">Loading...</p>
        ) : (
          <p id="total-profit" className="mt-1 text-3xl font-semibold text-[#2d2418]">
            {totalProfit === null ? "-" : totalProfit.toLocaleString()}
          </p>
        )}
      </div>
    </section>
  );
}
