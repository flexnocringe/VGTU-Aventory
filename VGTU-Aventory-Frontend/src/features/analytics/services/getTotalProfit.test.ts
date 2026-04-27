import { getTotalProfit } from "@/features/analytics/services/getTotalProfit";

describe("getTotalProfit", () => {
  const mockFetch = jest.fn();

  beforeEach(() => {
    global.fetch = mockFetch as unknown as typeof fetch;
    mockFetch.mockReset();
  });

  it("returns total profit when request succeeds", async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ totalProfit: 1250 }),
    } as Response);

    const result = await getTotalProfit({
      startDate: "2026-03-01",
      endDate: "2026-03-17",
    });

    expect(mockFetch).toHaveBeenCalledWith(
      "/api/analytics/total-profit?startDate=2026-03-01&endDate=2026-03-17",
      {
        method: "GET",
        cache: "no-store",
      },
    );
    expect(result).toEqual({ totalProfit: 1250 });
  });

  it("throws an error when response is not ok", async () => {
    mockFetch.mockResolvedValue({ ok: false } as Response);

    await expect(
      getTotalProfit({ startDate: "2026-03-01", endDate: "2026-03-17" }),
    ).rejects.toThrow("Failed to fetch total profit.");
  });
});
