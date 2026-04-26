"use client";

import { createContext, useState, ReactNode, useContext, useCallback, useEffect } from "react";

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
  error: string | null;
};

const AnalyticsDatesContext = createContext<AnalyticsDatesContextType | undefined>(undefined);

export function AnalyticsDatesProvider({ children }: { children: ReactNode }) {
  const today = getTodayIsoDate();
  const [dateRange, setDateRange] = useState<AnalyticsDateRange>({
    startDate: today,
    endDate: today,
  });
  const [error, setError] = useState<string | null>(null);

  const handleSetDateRange = useCallback((dates: AnalyticsDateRange) => {
    if (dates.startDate > dates.endDate) {
      setError("Start date cannot be later than end date.");
      return;
    }

    if (dates.endDate > today) {
      setError("End date cannot be in the future.");
      return;
    }

    setError(null);
    setDateRange(dates);
  }, [today]);

  return (
    <AnalyticsDatesContext.Provider value={{ dateRange, setDateRange: handleSetDateRange, error }}>
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
