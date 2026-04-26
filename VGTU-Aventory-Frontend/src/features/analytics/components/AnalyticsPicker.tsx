"use client";

import { FormEvent } from "react";
import { useAnalyticsDates } from "@/features/analytics/context/AnalyticsDatesContext";

function getTodayIsoDate(): string {
  return new Date().toISOString().split("T")[0];
}

export function AnalyticsPicker() {
  const today = getTodayIsoDate();
  const { dateRange, setDateRange, error } = useAnalyticsDates();

  const handleChangeStartDate = (value: string) => {
    setDateRange({ startDate: value, endDate: dateRange.endDate });
  };

  const handleChangeEndDate = (value: string) => {
    setDateRange({ startDate: dateRange.startDate, endDate: value });
  };

  return (
    <section className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)] sm:p-8">
      <h2 className="text-2xl font-semibold text-[#2d2418]">Date range</h2>
      <p className="mt-2 text-sm text-[#6a5841]">Select dates to update all analytics below.</p>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <label className="space-y-1">
          <span className="text-sm font-medium text-[#5b4a37]">Start date</span>
          <input
            id="analytics-start-date"
            type="date"
            value={dateRange.startDate}
            onChange={(event) => handleChangeStartDate(event.target.value)}
            className="w-full rounded-lg border border-[#e9dfcc] px-3 py-2 text-[#2d2418] outline-none ring-offset-2 transition focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
            required
          />
        </label>

        <label className="space-y-1">
          <span className="text-sm font-medium text-[#5b4a37]">End date</span>
          <input
            id="analytics-end-date"
            type="date"
            max={today}
            value={dateRange.endDate}
            onChange={(event) => handleChangeEndDate(event.target.value)}
            className="w-full rounded-lg border border-[#e9dfcc] px-3 py-2 text-[#2d2418] outline-none ring-offset-2 transition focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
            required
          />
        </label>
      </div>

      {error ? <p className="mt-4 text-sm text-red-600">{error}</p> : null}
    </section>
  );
}
