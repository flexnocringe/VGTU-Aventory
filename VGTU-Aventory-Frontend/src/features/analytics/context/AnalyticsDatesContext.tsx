"use client";

import { createContext, useState, ReactNode, useContext, useCallback, useEffect } from "react";
import { getAnalyticsDateBounds } from "@/features/analytics/services/getAnalyticsDateBounds";

function getTodayIsoDate(): string {
  return new Date().toISOString().split("T")[0];
}

export type AnalyticsDateRange = {
  startDate: string;
  endDate: string;
};

type AnalyticsDatesContextType = {
  dateRange: AnalyticsDateRange;
  setDateRange: (dates: AnalyticsDateRange) => void;
  minStartDate: string;
  maxEndDate: string;
  error: string | null;
};

const AnalyticsDatesContext = createContext<AnalyticsDatesContextType | undefined>(undefined);

export function AnalyticsDatesProvider({ children }: { children: ReactNode }) {
  const today = getTodayIsoDate();
  const [minStartDate, setMinStartDate] = useState(today);
  const [maxEndDate] = useState(today);
  const [dateRange, setDateRange] = useState<AnalyticsDateRange>({
    startDate: today,
    endDate: today,
  });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadDateBounds() {
      try {
        const bounds = await getAnalyticsDateBounds();
        if (!isMounted) {
          return;
        }

        setMinStartDate(bounds.firstSaleDate);
        setDateRange((current) => {
          const nextStartDate = current.startDate < bounds.firstSaleDate ? bounds.firstSaleDate : current.startDate;
          const nextEndDate = current.endDate < nextStartDate ? nextStartDate : current.endDate;
          return {
            startDate: nextStartDate,
            endDate: nextEndDate,
          };
        });
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        const message =
          requestError instanceof Error
            ? requestError.message
            : "Failed to load analytics date bounds.";
        setError(message);
      }
    }

    loadDateBounds();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleSetDateRange = useCallback((dates: AnalyticsDateRange) => {
    if (dates.startDate < minStartDate) {
      setError("Start date cannot be earlier than first recorded sale date.");
      return;
    }

    if (dates.startDate > dates.endDate) {
      setError("Start date cannot be later than end date.");
      return;
    }

    if (dates.endDate > maxEndDate) {
      setError("End date cannot be in the future.");
      return;
    }

    setError(null);
    setDateRange(dates);
  }, [maxEndDate, minStartDate]);

  return (
    <AnalyticsDatesContext.Provider
      value={{ dateRange, setDateRange: handleSetDateRange, minStartDate, maxEndDate, error }}
    >
      {children}
    </AnalyticsDatesContext.Provider>
  );
}

export function useAnalyticsDates(): AnalyticsDatesContextType {
  const context = useContext(AnalyticsDatesContext);
  if (!context) {
    throw new Error("useAnalyticsDates must be used within AnalyticsDatesProvider");
  }
  return context;
}
