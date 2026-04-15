import { apiUrl } from "@/lib/api/url";
import { ProductWithOwner } from "../types/sale";
import { fetchWithSession } from "@/features/auth/services/fetchWithSession";

export async function getSalesProducts(): Promise<ProductWithOwner[]> {
    const res = await fetchWithSession(apiUrl("/api/products/all"));

    if (!res.ok) {
        throw new Error(`Failed to fetch products: ${res.status}`);
    }

    return res.json();
}
