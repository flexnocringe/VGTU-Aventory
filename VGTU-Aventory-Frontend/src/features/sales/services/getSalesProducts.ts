import { apiUrl } from "@/lib/api/url";
import { createAuthHeaders } from "@/features/auth/session";
import { ProductWithOwner } from "../types/sale";

export async function getSalesProducts(): Promise<ProductWithOwner[]> {
    const res = await fetch(apiUrl("/api/products/all"), {
        headers: createAuthHeaders(),
    });

    if (!res.ok) {
        throw new Error(`Failed to fetch products: ${res.status}`);
    }

    return res.json();
}
