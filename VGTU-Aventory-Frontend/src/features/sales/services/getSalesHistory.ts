import { apiUrl } from "@/lib/api/url";
import { ProductWithOwner } from "../sales/types/sale";

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
    const res = await fetch(apiUrl("/sales"));

    if (!res.ok) {
        throw new Error(`Failed to fetch sales history: ${res.status}`);
    }

    return res.json();
}
