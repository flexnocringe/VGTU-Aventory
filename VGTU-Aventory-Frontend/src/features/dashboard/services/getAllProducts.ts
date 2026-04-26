import { ApiProduct, Product, mapApiProduct } from "@/features/dashboard/types/product";
import { apiUrl } from "@/lib/api/url";
import { fetchWithSession } from "@/features/auth/services/fetchWithSession";

export async function getAllProducts(signal?: AbortSignal): Promise<Product[]> {
  const response = await fetchWithSession(apiUrl("/api/products/all"), {
    signal,
  });

  if (!response.ok) {
    throw new Error(`API returned ${response.status} ${response.statusText}`);
  }

  const data = (await response.json()) as ApiProduct[];
  return data.map(mapApiProduct);
}