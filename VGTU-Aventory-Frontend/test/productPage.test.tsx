import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ProductsPage from "@/app/(dashboard)/products/page";

describe("ProductsPage", () => {
  const mockFetch = jest.fn();
  const originalApiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;
  const originalConfirm = global.confirm;

  beforeAll(() => {
    process.env.NEXT_PUBLIC_API_BASE_URL = "http://localhost";
  });

  beforeEach(() => {
    global.fetch = mockFetch as unknown as typeof fetch;
    mockFetch.mockReset();
    global.confirm = jest.fn(() => true);
  });

  afterAll(() => {
    process.env.NEXT_PUBLIC_API_BASE_URL = originalApiBaseUrl;
    global.confirm = originalConfirm;
  });

  it("loads and displays products", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [
        {
          productId: 1,
          productName: "Laptop",
          quantity: 5,
          price: 1000,
          productDescription: "Business laptop",
          photoUrl: "https://example.com/laptop.jpg",
          qrCode: "QR-1",
        },
      ],
    } as Response);

    render(<ProductsPage />);

    expect(screen.getByText("Loading products...")).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText("Laptop")).toBeInTheDocument();
      expect(screen.getByText("5")).toBeInTheDocument();
      expect(screen.getByText("$1000.00")).toBeInTheDocument();
      expect(screen.getByText("QR-1")).toBeInTheDocument();
    });
  });

  it("opens the product overview when clicking a row", async () => {
    const user = userEvent.setup();

    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [
          {
            productId: 1,
            productName: "Laptop",
            quantity: 5,
            price: 1000,
            productDescription: "Business laptop",
            photoUrl: "https://example.com/laptop.jpg",
            qrCode: "QR-1",
          },
        ],
      } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByText("Laptop")).toBeInTheDocument();
    });

    await user.click(screen.getByText("Laptop"));

    expect(screen.getByText("Product Overview #1")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Laptop")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Business laptop")).toBeInTheDocument();
    expect(screen.getByDisplayValue("https://example.com/laptop.jpg")).toBeInTheDocument();
    expect(screen.getByDisplayValue("QR-1")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Close" }));

    await waitFor(() => {
      expect(screen.queryByText("Product Overview #1")).not.toBeInTheDocument();
    });
  });

  it("selects all products and updates delete selected count", async () => {
    const user = userEvent.setup();

    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [
        {
          productId: 1,
          productName: "Laptop",
          quantity: 5,
          price: 1000,
          productDescription: "Business laptop",
          photoUrl: "https://example.com/laptop.jpg",
          qrCode: "QR-1",
        },
        {
          productId: 2,
          productName: "Mouse",
          quantity: 10,
          price: 50,
          productDescription: "Wireless mouse",
          photoUrl: "https://example.com/mouse.jpg",
          qrCode: "QR-2",
        },
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
          {
            productId: 1,
            productName: "Laptop",
            quantity: 5,
            price: 1000,
            productDescription: "Business laptop",
            photoUrl: "https://example.com/laptop.jpg",
            qrCode: "QR-1",
          },
          {
            productId: 2,
            productName: "Mouse",
            quantity: 10,
            price: 50,
            productDescription: "Wireless mouse",
            photoUrl: "https://example.com/mouse.jpg",
            qrCode: "QR-2",
          },
        ],
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByLabelText("Select product 1")).toBeInTheDocument();
    });

    await user.click(screen.getByLabelText("Select product 1"));
    await user.click(screen.getByLabelText("Select product 2"));
    await user.click(screen.getByRole("button", { name: /Delete selected \(2\)/ }));

    await waitFor(() => {
      const deleteCalls = mockFetch.mock.calls.filter(
        ([url, init]) =>
          String(url).includes("/api/products/") &&
          typeof init === "object" &&
          init !== null &&
          "method" in init &&
          (init as RequestInit).method === "DELETE",
      );

      expect(deleteCalls).toHaveLength(2);
      expect(deleteCalls.some(([url]) => String(url).includes("/api/products/1"))).toBe(true);
      expect(deleteCalls.some(([url]) => String(url).includes("/api/products/2"))).toBe(true);
    });

    await waitFor(() => {
      expect(screen.queryByText("Laptop")).not.toBeInTheDocument();
      expect(screen.queryByText("Mouse")).not.toBeInTheDocument();
    });
  });

  it("saves edited product from modal", async () => {
    const user = userEvent.setup();

    mockFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [
          {
            productId: 1,
            productName: "Laptop",
            quantity: 5,
            price: 1000,
            productDescription: "Business laptop",
            photoUrl: "https://example.com/laptop.jpg",
            qrCode: "QR-1",
          },
        ],
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [
          {
            productId: 1,
            productName: "Laptop Pro",
            quantity: 7,
            price: 1250,
            productDescription: "Business laptop",
            photoUrl: "https://example.com/laptop.jpg",
            qrCode: "QR-1",
          },
        ],
      } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "Edit" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "Edit" }));

    const nameInput = screen.getByDisplayValue("Laptop");
    await user.clear(nameInput);
    await user.type(nameInput, "Laptop Pro");

    const quantityInput = screen.getByDisplayValue("5");
    await user.clear(quantityInput);
    await user.type(quantityInput, "7");

    const priceInput = screen.getByDisplayValue("1000");
    await user.clear(priceInput);
    await user.type(priceInput, "1250");

    await user.click(screen.getByRole("button", { name: "Save Changes" }));

    await waitFor(() => {
      const putCall = mockFetch.mock.calls.find(
        ([url, init]) =>
          String(url).includes("/api/products/1") &&
          typeof init === "object" &&
          init !== null &&
          "method" in init &&
          (init as RequestInit).method === "PUT",
      );

      expect(putCall).toBeDefined();
      const requestInit = putCall?.[1] as RequestInit;
      const body = JSON.parse(String(requestInit.body));
      expect(body.productName).toBe("Laptop Pro");
      expect(body.quantity).toBe(7);
      expect(body.price).toBe(1250);
    });
  });

  it("shows error when product load fails", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 500,
    } as Response);

    render(<ProductsPage />);

    await waitFor(() => {
      expect(screen.getByText("API error: 500")).toBeInTheDocument();
    });
  });
});
