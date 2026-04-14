import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { AnalyticsProfitCalculator } from "@/features/analytics/components/AnalyticsProfitCalculator";
import { getTotalProfit } from "@/features/analytics/services/getTotalProfit";

jest.mock("@/features/analytics/services/getTotalProfit", () => ({
  getTotalProfit: jest.fn(),
}));

const mockedGetTotalProfit = getTotalProfit as jest.MockedFunction<typeof getTotalProfit>;

describe("AnalyticsProfitCalculator", () => {
  beforeEach(() => {
    mockedGetTotalProfit.mockReset();
  });

  it("renders initial state", () => {
    render(<AnalyticsProfitCalculator />);

    expect(screen.getByRole("heading", { name: "Analytics" })).toBeInTheDocument();
    expect(screen.getByText("Total profit")).toBeInTheDocument();
    expect(screen.getByText("-")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Calculate" })).toBeInTheDocument();
  });

  it("submits selected dates and shows calculated profit", async () => {
    mockedGetTotalProfit.mockResolvedValue({ totalProfit: 1500 });

    render(<AnalyticsProfitCalculator />);

    fireEvent.change(screen.getByLabelText("Start date"), {
      target: { value: "2026-03-01" },
    });
    fireEvent.change(screen.getByLabelText("End date"), {
      target: { value: "2026-03-17" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Calculate" }));

    await waitFor(() => {
      expect(mockedGetTotalProfit).toHaveBeenCalledWith({
        startDate: "2026-03-01",
        endDate: "2026-03-17",
      });
    });

    await waitFor(() => {
      expect(screen.getByText(/1[\s,]500/)).toBeInTheDocument();
    });
  });

  it("shows an error when calculation request fails", async () => {
    mockedGetTotalProfit.mockRejectedValue(new Error("Request failed."));

    render(<AnalyticsProfitCalculator />);

    fireEvent.click(screen.getByRole("button", { name: "Calculate" }));

    await waitFor(() => {
      expect(screen.getByText("Request failed.")).toBeInTheDocument();
    });

    expect(screen.getByText("-")).toBeInTheDocument();
  });
});
