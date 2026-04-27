export type SaleType = "SALE" | "RETURN";

export interface SaleRequest {
    productId: number;
    ownerId: number;
    quantity: number;
    saleLocation: string;
    saleType: SaleType;
    saleNote: string;
}

export interface Sale extends SaleRequest {
    id: number;
    saleProductPrice: number;
    totalPrice: number;
    createdAt?: string;
}

export interface User {
    id: number;
    email: string;
    password?: string;
    role: string;
}

export interface ProductWithOwner {
    productId: number;
    productName: string;
    owner: User | null;
    price: number;
    quantity: number;
}
