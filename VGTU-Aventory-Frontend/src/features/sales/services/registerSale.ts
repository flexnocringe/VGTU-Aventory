import { apiUrl } from "@/lib/api/url";
import { Sale, SaleRequest } from "../types/sale";
import { createAuthHeaders } from "@/features/auth/session";

export async function registerSale(request: SaleRequest): Promise<Sale> {
    const res = await fetch(apiUrl("/sales"), {
        method: "POST",
        headers: createAuthHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify(request),
    });

    if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || `Failed to register sale: ${res.status}`);
    }

    return res.json();
}
