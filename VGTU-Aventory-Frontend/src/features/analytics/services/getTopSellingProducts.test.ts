import { getTopSellingProducts } from "@/features/analytics/services/getTopSellingProducts";

describe("getTopSellingProducts", () => {
  const mockFetch = jest.fn();

  beforeEach(() => {
    global.fetch = mockFetch as unknown as typeof fetch;
    mockFetch.mockReset();
  });

  it("returns top selling products when request succeeds", async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => [
        { productId: 4, productName: "Apple", salesCount: 9 },
        { productId: 2, productName: "Banana", salesCount: 4 },
      ],
    } as Response);

    const result = await getTopSellingProducts({
      startDate: "2026-03-01",
      endDate: "2026-03-17",
    });

    expect(mockFetch).toHaveBeenCalledWith(
      "/api/analytics/top-selling/2026-03-01/2026-03-17",
      {
        method: "GET",
        cache: "no-store",
      },
    );

    expect(result).toEqual([
      { productId: 4, productName: "Apple", salesCount: 9 },
      { productId: 2, productName: "Banana", salesCount: 4 },
    ]);
  });

  it("throws backend error message when response is not ok", async () => {
    mockFetch.mockResolvedValue({
      ok: false,
      json: async () => ({ error: "Start date cannot be earlier than first recorded sale date" }),
    } as Response);

    await expect(
      getTopSellingProducts({ startDate: "2026-01-01", endDate: "2026-03-17" }),
    ).rejects.toThrow("Start date cannot be earlier than first recorded sale date");
  });
});
