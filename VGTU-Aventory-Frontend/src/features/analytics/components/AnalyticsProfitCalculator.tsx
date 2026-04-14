"use client";

import { FormEvent, useEffect, useState } from "react";
import { getTotalProfit } from "@/features/analytics/services/getTotalProfit";

function getTodayIsoDate(): string {
  return new Date().toISOString().split("T")[0];
}

export function AnalyticsProfitCalculator() {
  const today = getTodayIsoDate();
  const startDateLaterThanEndDateErrorMessage = "Start date cannot be later than end date.";

  const [startDate, setStartDate] = useState(today);
  const [endDate, setEndDate] = useState(today);
  const [totalProfit, setTotalProfit] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (startDate > endDate) {
      setError(startDateLaterThanEndDateErrorMessage);
      return;
    }

    setError(null);
  }, [startDate, endDate]);

  async function handleCalculate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (startDate > endDate) {
      return;
    }

    setIsLoading(true);

    try {
      const data = await getTotalProfit({ startDate, endDate });
      setTotalProfit(data.totalProfit);
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

  return (
    <section className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)] sm:p-8">
      <h1 className="text-2xl font-semibold text-[#2d2418]">Analytics</h1>
      <p className="mt-2 text-sm text-[#6a5841]">
        Select a date range and calculate total profit.
      </p>

      <form className="mt-6 space-y-4" onSubmit={handleCalculate}>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <label className="space-y-1">
            <span className="text-sm font-medium text-[#5b4a37]">Start date</span>
            <input
              id="start-date"
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
              id="end-date"
              type="date"
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
          {isLoading ? "Calculating..." : "Calculate"}
        </button>
      </form>

      <div className="mt-6 rounded-xl bg-[#fff4e2] p-4">
        <p className="text-sm text-[#8a6b45]">Total profit</p>
        <p id="total-profit" className="mt-1 text-3xl font-semibold text-[#2d2418]">
          {totalProfit === null ? "-" : totalProfit.toLocaleString()}
        </p>
        {error ? <p id="error-message" className="mt-2 text-sm text-red-600">{error}</p> : null}
      </div>
    </section>
  );
}
