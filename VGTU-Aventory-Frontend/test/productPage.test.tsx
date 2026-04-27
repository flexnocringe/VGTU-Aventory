import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ProductsPage from "@/app/(dashboard)/products/page";

jest.mock(
    "qrcode.react",
    () => ({
        QRCodeCanvas: () => null,
    }),
    { virtual: true },
);

describe("ProductsPage", () => {
    const mockFetch = jest.fn();
    const originalApiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;
    const originalConfirm = global.confirm;

    const mockResponse = (body: unknown, ok = true, status = 200) =>
        ({
            ok,
            status,
            json: async () => body,
            text: async () => (typeof body === "string" ? body : JSON.stringify(body)),
        }) as Response;

    const installApiMock = () => {
        mockFetch.mockImplementation((input, init) => {
            const url = String(input);
            const method = (init as RequestInit | undefined)?.method ?? "GET";

            if (url.includes("/api/products/all")) {
                return Promise.resolve(
                    mockResponse([
                        {
                            productId: 1,
                            productName: "Laptop",
                            quantity: 5,
                            price: 1000,
                            productDescription: "Business laptop",
                            photoUrl: "https://example.com/laptop.jpg",
                            qrCode: "QR-1",
                            categoryId: 10,
                            categoryName: "Electronics",
                        },
                    ]),
                );
            }

            if (url.includes("/api/category/all")) {
                return Promise.resolve(
                    mockResponse([
                        {
                            categoryId: 10,
                            categoryName: "Electronics",
                        },
                    ]),
                );
            }

            if (url.includes("/api/category") && method === "POST") {
                return Promise.resolve(
                    mockResponse({
                        categoryId: 11,
                        categoryName: "Accessories",
                    }, true, 201),
                );
            }

            if (url.includes("/api/products") && method === "POST") {
                return Promise.resolve(mockResponse({}, true, 201));
            }

            return Promise.resolve(mockResponse({}));
        });
    };

    beforeAll(() => {
        process.env.NEXT_PUBLIC_API_BASE_URL = "http://localhost";
    });

    beforeEach(() => {
        global.fetch = mockFetch as unknown as typeof fetch;
        mockFetch.mockReset();
        global.confirm = jest.fn(() => true);
        installApiMock();
    });

    afterAll(() => {
        process.env.NEXT_PUBLIC_API_BASE_URL = originalApiBaseUrl;
        global.confirm = originalConfirm;
    });

    it("loads products and category options", async () => {
        const user = userEvent.setup();

        render(<ProductsPage />);

        expect(screen.getByText("Loading products...")).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText("Laptop")).toBeInTheDocument();
        });

        const addProductButton = screen.getByRole("button", { name: "Add Product" });
        await user.click(addProductButton);

        expect(screen.getByRole("combobox", { name: "Category" })).toBeInTheDocument();
        expect(screen.getByRole("option", { name: "Electronics" })).toBeInTheDocument();
    });

    it("creates a category and shows a success message", async () => {
        const user = userEvent.setup();

        render(<ProductsPage />);

        await waitFor(() => {
            expect(screen.getByText("Laptop")).toBeInTheDocument();
        });

        await user.click(screen.getByRole("button", { name: "Add Category" }));
        await user.type(screen.getByLabelText("Category Name"), "Accessories");
        await user.click(screen.getAllByRole("button", { name: "Add Category" })[1]);

        await waitFor(() => {
            expect(screen.getByText(/Category "Accessories" created successfully!/)).toBeInTheDocument();
        });

        expect(mockFetch).toHaveBeenCalledWith(
            expect.stringContaining("/api/category"),
            expect.objectContaining({ method: "POST" }),
        );
    });
});