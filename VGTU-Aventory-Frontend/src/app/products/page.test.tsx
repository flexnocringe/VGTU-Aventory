import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ProductsPage from "@/app/products/page";

describe("ProductsPage", () => {
  const mockFetch = jest.fn();

  beforeEach(() => {
    global.fetch = mockFetch as unknown as typeof fetch;
    mockFetch.mockReset();
  });

  it("loads and displays products", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [
        { productId: 1, name: "Laptop", price: 1200 },
        { productId: 2, name: "Mouse", price: 25 },
      ],
    } as Response);

    render(<ProductsPage />);

    expect(screen.getByText("Loading products...")).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "Products" })).toBeInTheDocument();
      expect(screen.getByText("Laptop")).toBeInTheDocument();
      expect(screen.getByText("Mouse")).toBeInTheDocument();
    });
  });

  it("selects all products and updates delete selected count", async () => {
    const user = userEvent.setup();

    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [
        { productId: 1, name: "Laptop" },
        { productId: 2, name: "Mouse" },
      ],
    } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByLabelText("Select all products")).toBeInTheDocument();
    });

    await user.click(screen.getByLabelText("Select all products"));

    expect(screen.getByRole("button", { name: /Delete selected \(2\)/ })).toBeInTheDocument();
  });

  it("deletes selected products and refreshes list", async () => {
    const user = userEvent.setup();

    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [
          { productId: 1, name: "Laptop" },
          { productId: 2, name: "Mouse" },
        ],
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [{ productId: 2, name: "Mouse" }],
      } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByLabelText("Select product 1")).toBeInTheDocument();
    });

    await user.click(screen.getByLabelText("Select product 1"));
    await user.click(screen.getByRole("button", { name: /Delete selected \(1\)/ }));

    await waitFor(() => {
      expect(mockFetch).toHaveBeenNthCalledWith(2, "/api/backend/deleteProducts", {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ productId: [1] }),
      });
    });

    await waitFor(() => {
      expect(screen.queryByText("Laptop")).not.toBeInTheDocument();
      expect(screen.getByText("Mouse")).toBeInTheDocument();
    });
  });

  it("shows error message when initial fetch fails", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 500,
    } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByText("Failed to fetch products: 500")).toBeInTheDocument();
    });
  });
});
