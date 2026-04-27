import { render, screen, waitFor } from "@testing-library/react";
import { AnalyticsProfitCalculator } from "@/features/analytics/components/AnalyticsProfitCalculator";
import { getTotalProfit } from "@/features/analytics/services/getTotalProfit";
import { getTotalSalesCount } from "@/features/analytics/services/getTotalSalesCount";
import { getAnalyticsDateBounds } from "@/features/analytics/services/getAnalyticsDateBounds";
import { AnalyticsDatesProvider } from "@/features/analytics/context/AnalyticsDatesContext";

jest.mock("@/features/analytics/services/getTotalProfit", () => ({
  getTotalProfit: jest.fn(),
}));

jest.mock("@/features/analytics/services/getTotalSalesCount", () => ({
  getTotalSalesCount: jest.fn(),
}));

jest.mock("@/features/analytics/services/getAnalyticsDateBounds", () => ({
  getAnalyticsDateBounds: jest.fn(),
}));

const mockedGetTotalProfit = getTotalProfit as jest.MockedFunction<typeof getTotalProfit>;
const mockedGetTotalSalesCount = getTotalSalesCount as jest.MockedFunction<typeof getTotalSalesCount>;
const mockedGetAnalyticsDateBounds = getAnalyticsDateBounds as jest.MockedFunction<typeof getAnalyticsDateBounds>;

describe("AnalyticsProfitCalculator", () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2026-04-26T10:00:00.000Z"));
    mockedGetTotalProfit.mockReset();
    mockedGetTotalSalesCount.mockReset();
    mockedGetAnalyticsDateBounds.mockReset();
    mockedGetAnalyticsDateBounds.mockResolvedValue({
      firstSaleDate: "2026-01-01",
      today: "2026-04-26",
    });
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  function renderWithProvider() {
    return render(
      <AnalyticsDatesProvider>
        <AnalyticsProfitCalculator />
      </AnalyticsDatesProvider>,
    );
  }

  it("renders initial state", async () => {
    mockedGetTotalProfit.mockImplementation(
      () => new Promise(() => {
        // Keep the request pending so the component remains in loading state for this assertion.
      }),
    );
    mockedGetTotalSalesCount.mockImplementation(
      () => new Promise(() => {
        // Keep the request pending so the component remains in loading state for this assertion.
      }),
    );

    renderWithProvider();

    expect(screen.getByRole("heading", { name: "Sales Summary" })).toBeInTheDocument();
    expect(screen.getByText("Total profit")).toBeInTheDocument();
    expect(screen.getByText("Total sales count")).toBeInTheDocument();
    const loadingLabels = await screen.findAllByText("Loading...");
    expect(loadingLabels).toHaveLength(2);
  });

  it("loads total profit for current date range and shows result", async () => {
    mockedGetTotalProfit.mockResolvedValue({ totalProfit: 1500 });
    mockedGetTotalSalesCount.mockResolvedValue({ totalSalesCount: 42 });

    renderWithProvider();

    await waitFor(() => {
      expect(mockedGetTotalProfit).toHaveBeenCalledWith({
        startDate: "2026-04-26",
        endDate: "2026-04-26",
      });
    });

    await waitFor(() => {
      expect(screen.getByText(/1[\s,]500/)).toBeInTheDocument();
    });

    await waitFor(() => {
      expect(screen.getByText(/42/)).toBeInTheDocument();
    });
  });

  it("shows an error when calculation request fails", async () => {
    mockedGetTotalProfit.mockRejectedValue(new Error("Request failed."));
    mockedGetTotalSalesCount.mockResolvedValue({ totalSalesCount: 10 });

    renderWithProvider();

    const errors = await screen.findAllByText("Request failed.");
    expect(errors).toHaveLength(2);

    expect(screen.queryByText("-")).not.toBeInTheDocument();
  });
});
