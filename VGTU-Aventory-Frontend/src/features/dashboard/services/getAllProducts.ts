import { ApiProduct, Product, mapApiProduct } from "@/features/dashboard/types/product";

const PRODUCTS_URL = "http://localhost:8080/api/products/all";

export async function getAllProducts(signal?: AbortSignal): Promise<Product[]> {
  const response = await fetch(PRODUCTS_URL, { signal });

  if (!response.ok) {
    throw new Error(`API returned ${response.status} ${response.statusText}`);
  }

  const data = (await response.json()) as ApiProduct[];
  return data.map(mapApiProduct);
}
