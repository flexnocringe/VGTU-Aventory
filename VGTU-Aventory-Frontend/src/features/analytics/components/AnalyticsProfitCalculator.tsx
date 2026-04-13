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
    <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
      <h1 className="text-2xl font-semibold text-slate-900">Analytics</h1>
      <p className="mt-2 text-sm text-slate-600">
        Select a date range and calculate total profit.
      </p>

      <form className="mt-6 space-y-4" onSubmit={handleCalculate}>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <label className="space-y-1">
            <span className="text-sm font-medium text-slate-700">Start date</span>
            <input
              id="start-date"
              type="date"
              value={startDate}
              onChange={(event) => setStartDate(event.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 outline-none ring-offset-2 transition focus:border-slate-400 focus:ring-2 focus:ring-slate-300"
              required
            />
          </label>

          <label className="space-y-1">
            <span className="text-sm font-medium text-slate-700">End date</span>
            <input
              id="end-date"
              type="date"
              value={endDate}
              onChange={(event) => setEndDate(event.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 outline-none ring-offset-2 transition focus:border-slate-400 focus:ring-2 focus:ring-slate-300"
              required
            />
          </label>
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="inline-flex items-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {isLoading ? "Calculating..." : "Calculate"}
        </button>
      </form>

      <div className="mt-6 rounded-xl bg-slate-50 p-4">
        <p className="text-sm text-slate-600">Total profit</p>
        <p id="total-profit" className="mt-1 text-3xl font-semibold text-slate-900">
          {totalProfit === null ? "-" : totalProfit.toLocaleString()}
        </p>
        {error ? <p id="error-message" className="mt-2 text-sm text-red-600">{error}</p> : null}
      </div>
    </section>
  );
}
