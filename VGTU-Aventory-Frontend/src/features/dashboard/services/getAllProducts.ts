import { ApiProduct, Product, mapApiProduct } from "@/features/dashboard/types/product";
import { createAuthHeaders } from "@/features/auth/session";
import { apiUrl } from "@/lib/api/url";

export async function getAllProducts(signal?: AbortSignal): Promise<Product[]> {
  const response = await fetch(apiUrl("/api/products/all"), {
    signal,
    headers: createAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error(`API returned ${response.status} ${response.statusText}`);
  }

  const data = (await response.json()) as ApiProduct[];
  return data.map(mapApiProduct);
}