import { getTotalSalesCount } from "@/features/analytics/services/getTotalSalesCount";

describe("getTotalSalesCount", () => {
  const mockFetch = jest.fn();

  beforeEach(() => {
    global.fetch = mockFetch as unknown as typeof fetch;
    mockFetch.mockReset();
  });

  it("returns total sales count when request succeeds", async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ totalSalesCount: 37 }),
    } as Response);

    const result = await getTotalSalesCount({
      startDate: "2026-03-01",
      endDate: "2026-03-17",
    });

    expect(mockFetch).toHaveBeenCalledWith(
      "/api/analytics/total-sales-count?startDate=2026-03-01&endDate=2026-03-17",
      {
        method: "GET",
        cache: "no-store",
      },
    );

    expect(result).toEqual({ totalSalesCount: 37 });
  });

  it("throws backend error message when response is not ok", async () => {
    mockFetch.mockResolvedValue({
      ok: false,
      json: async () => ({ error: "Start date cannot be earlier than first recorded sale date." }),
    } as Response);

    await expect(
      getTotalSalesCount({ startDate: "2026-01-01", endDate: "2026-03-17" }),
    ).rejects.toThrow("Start date cannot be earlier than first recorded sale date.");
  });
});
