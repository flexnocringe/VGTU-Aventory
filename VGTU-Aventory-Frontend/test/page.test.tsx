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
      json: async () => [{ productId: 1, productName: "Laptop", quantity: 5 }],
    } as Response);

    render(<ProductsPage />);

    expect(screen.getByText("Loading products...")).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText("Laptop")).toBeInTheDocument();
      expect(screen.getByText("5")).toBeInTheDocument();
    });
  });

  it("saves edited product from modal", async () => {
    const user = userEvent.setup();

    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [{ productId: 1, productName: "Laptop", quantity: 5, price: 1000 }],
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [{ productId: 1, productName: "Laptop Pro", quantity: 5, price: 1000 }],
      } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "Edit" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "Edit" }));

    const nameInput = screen.getByLabelText("productName");
    await user.clear(nameInput);
    await user.type(nameInput, "Laptop Pro");

    await user.click(screen.getByRole("button", { name: "Save Changes" }));

    await waitFor(() => {
      const putCall = mockFetch.mock.calls.find(
        ([url, init]) =>
          url === "/api/backend/editProduct" &&
          typeof init === "object" &&
          init !== null &&
          "method" in init &&
          (init as RequestInit).method === "PUT",
      );

      expect(putCall).toBeDefined();
      const requestInit = putCall?.[1] as RequestInit;
      const body = JSON.parse(String(requestInit.body));
      expect(body.productName).toBe("Laptop Pro");
    });
  });

  it("commits inline edit on Enter", async () => {
    const user = userEvent.setup();

    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [{ productId: 1, productName: "Laptop", quantity: 5, price: 1000 }],
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [{ productId: 1, productName: "Laptop", quantity: 7, price: 1000 }],
      } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByText("5")).toBeInTheDocument();
    });

    await user.dblClick(screen.getByText("5"));

    const input = screen.getByDisplayValue("5");
    await user.clear(input);
    await user.type(input, "7{Enter}");

    await waitFor(() => {
      const putCall = mockFetch.mock.calls.find(
        ([url, init]) =>
          url === "/api/backend/editProduct" &&
          typeof init === "object" &&
          init !== null &&
          "method" in init &&
          (init as RequestInit).method === "PUT",
      );

      expect(putCall).toBeDefined();
      const requestInit = putCall?.[1] as RequestInit;
      const body = JSON.parse(String(requestInit.body));
      expect(body.quantity).toBe(7);
    });
  });

  it("shows error when product load fails", async () => {
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
