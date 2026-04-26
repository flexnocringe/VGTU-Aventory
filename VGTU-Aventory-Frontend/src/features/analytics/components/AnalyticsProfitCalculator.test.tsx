import { render, screen, waitFor } from "@testing-library/react";
import { AnalyticsProfitCalculator } from "@/features/analytics/components/AnalyticsProfitCalculator";
import { getTotalProfit } from "@/features/analytics/services/getTotalProfit";
import { AnalyticsDatesProvider } from "@/features/analytics/context/AnalyticsDatesContext";

jest.mock("@/features/analytics/services/getTotalProfit", () => ({
  getTotalProfit: jest.fn(),
}));

const mockedGetTotalProfit = getTotalProfit as jest.MockedFunction<typeof getTotalProfit>;

describe("AnalyticsProfitCalculator", () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2026-04-26T10:00:00.000Z"));
    mockedGetTotalProfit.mockReset();
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

  it("renders initial state", () => {
    mockedGetTotalProfit.mockImplementation(
      () => new Promise(() => {
        // Keep the request pending so the component remains in loading state for this assertion.
      }),
    );

    renderWithProvider();

    expect(screen.getByRole("heading", { name: "Total Profit" })).toBeInTheDocument();
    expect(screen.getByText("Total profit")).toBeInTheDocument();
    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  it("loads total profit for current date range and shows result", async () => {
    mockedGetTotalProfit.mockResolvedValue({ totalProfit: 1500 });

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
  });

  it("shows an error when calculation request fails", async () => {
    mockedGetTotalProfit.mockRejectedValue(new Error("Request failed."));

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByText("Request failed.")).toBeInTheDocument();
    });

    expect(screen.queryByText("-")).not.toBeInTheDocument();
  });
});
