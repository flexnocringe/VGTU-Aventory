import { apiUrl } from "@/lib/api/url";
import { createAuthHeaders } from "@/features/auth/session";

export interface SalesHistoryRecord {
    id: number;
    product: {
        productId: number;
        productName: string;
    };
    owner: {
        id: number;
        email: string;
    };
    quantity: number;
    saleLocation: string;
    saleProductPrice: number;
    totalPrice: number;
    saleNote: string;
    saleDate: string;
    saleType: "SALE" | "RETURN";
}

export async function getSalesHistory(): Promise<SalesHistoryRecord[]> {
    const res = await fetch(apiUrl("/sales"), {
        headers: createAuthHeaders(),
    });

    if (!res.ok) {
        throw new Error(`Failed to fetch sales history: ${res.status}`);
    }

    return res.json();
}
