import { registerUser } from "@/features/auth/services/registerUser";

describe("registerUser", () => {
  const mockFetch = jest.fn();

  beforeEach(() => {
    process.env.NEXT_PUBLIC_API_BASE_URL = "http://localhost:8080";
    global.fetch = mockFetch as unknown as typeof fetch;
    mockFetch.mockReset();
  });

  it("returns created user when request succeeds", async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ id: 1, email: "test@test.com", role: "SELLER" }),
    } as Response);

    const result = await registerUser({
      email: "test@test.com",
      password: "password123",
    });

    expect(mockFetch).toHaveBeenCalledWith("http://localhost:8080/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: "test@test.com", password: "password123" }),
    });

    expect(result).toEqual({ id: 1, email: "test@test.com", role: "SELLER" });
  });

  it("throws backend error text when response is not ok", async () => {
    mockFetch.mockResolvedValue({
      ok: false,
      text: async () => "Email already exists",
    } as Response);

    await expect(
      registerUser({ email: "test@test.com", password: "password123" }),
    ).rejects.toThrow("Email already exists");
  });
});
