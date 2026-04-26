"use client";

import { useEffect, useState } from "react";
import { registerSale } from "@/features/sales/services/registerSale";
import { getSalesProducts } from "@/features/sales/services/getSalesProducts";
import { SalesHistory } from "@/features/sales/components/SalesHistory";
import { ProductWithOwner, SaleType } from "@/features/sales/types/sale";

export default function SalesPage() {
    const [products, setProducts] = useState<ProductWithOwner[]>([]);
    const [loadingProducts, setLoadingProducts] = useState(true);
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const [form, setForm] = useState({
        productId: "",
        quantity: "",
        saleLocation: "",
        saleType: "SALE" as SaleType,
        saleNote: "",
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        const loadProducts = async () => {
            try {
                const data = await getSalesProducts();
                setProducts(data);
            } catch (err) {
                setError(err instanceof Error ? err.message : "Failed to load products");
            } finally {
                setLoadingProducts(false);
            }
        };

        loadProducts();
    }, [refreshTrigger]);

    const selectedProduct = products.find((p) => p.productId === parseInt(form.productId));
    const ownerId = selectedProduct?.owner?.id;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(false);

        try {
            if (!selectedProduct) {
                throw new Error("Please select a valid product");
            }

            if (!ownerId) {
                throw new Error(`Product "${selectedProduct.productName}" does not have an owner assigned`);
            }

            const quantity = parseInt(form.quantity);
            if (quantity <= 0) {
                throw new Error("Quantity must be greater than 0");
            }

            if (quantity > selectedProduct.quantity) {
                throw new Error(`Not enough stock. Available: ${selectedProduct.quantity}, Requested: ${quantity}`);
            }

            await registerSale({
                productId: parseInt(form.productId),
                ownerId: ownerId,
                quantity: quantity,
                saleLocation: form.saleLocation,
                saleType: form.saleType,
                saleNote: form.saleNote,
            });

            setSuccess(true);
            setForm({
                productId: "",
                quantity: "",
                saleLocation: "",
                saleType: "SALE",
                saleNote: "",
            });

            setRefreshTrigger((prev) => prev + 1);

            setTimeout(() => setSuccess(false), 3000);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to register sale");
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="min-h-screen bg-transparent p-6">
            <div className="mx-auto w-full max-w-6xl flex flex-col gap-6">
                <div className="max-w-2xl">
                    <header className="mb-6">
                    <h1 className="text-3xl font-bold text-[#2d2418]">Register Sale</h1>
                    <p className="mt-2 text-sm text-[#6a5841]">Record a new sale or return transaction</p>
                </header>

                {error && (
                    <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4 text-red-800">
                        <strong>Error:</strong> {error}
                    </div>
                )}

                {success && (
                    <div className="mb-6 rounded-xl border border-green-200 bg-green-50 p-4 text-green-800">
                        <strong>Success!</strong> Sale has been registered successfully
                    </div>
                )}

                <form onSubmit={handleSubmit} className="rounded-xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
                    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-[#5b4a37]">Product</label>
                            <select
                                name="productId"
                                value={form.productId}
                                onChange={handleChange}
                                disabled={loadingProducts}
                                className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20 disabled:bg-gray-100"
                                required
                            >
                                <option value="">
                                    {loadingProducts ? "Loading products..." : "Select a product"}
                                </option>
                                {products.map((product) => (
                                    <option key={product.productId} value={product.productId}>
                                        {product.productName} (${product.price.toFixed(2)}) - Stock: {product.quantity}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-[#5b4a37]">
                                Quantity {selectedProduct && <span className="text-[#8a6b45]">(max: {selectedProduct.quantity})</span>}
                            </label>
                            <input
                                type="number"
                                name="quantity"
                                value={form.quantity}
                                onChange={handleChange}
                                placeholder="Enter quantity"
                                min="1"
                                max={selectedProduct?.quantity}
                                className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-[#5b4a37]">Sale Type</label>
                            <select
                                name="saleType"
                                value={form.saleType}
                                onChange={handleChange}
                                className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
                            >
                                <option value="SALE">Sale</option>
                                <option value="RETURN">Return</option>
                            </select>
                        </div>

                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-[#5b4a37]">Sale Location</label>
                            <input
                                type="text"
                                name="saleLocation"
                                value={form.saleLocation}
                                onChange={handleChange}
                                placeholder="Enter sale location"
                                className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
                                required
                            />
                        </div>

                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-[#5b4a37]">Sale Note</label>
                            <textarea
                                name="saleNote"
                                value={form.saleNote}
                                onChange={handleChange}
                                placeholder="Add any notes about this sale (optional)"
                                rows={3}
                                className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
                            />
                        </div>
                    </div>

                    <div className="mt-6 flex gap-2">
                        <button
                            type="submit"
                            disabled={loading || !form.productId || !ownerId}
                            className="rounded-md bg-[#f59e0b] px-4 py-2 text-white shadow-sm transition disabled:bg-[#d4893e] hover:bg-[#ea8c08] hover:shadow-md disabled:cursor-not-allowed"
                        >
                            {loading ? "Registering..." : "Register Sale"}
                        </button>
                        <button
                            type="reset"
                            onClick={() => setForm({ productId: "", quantity: "", saleLocation: "", saleType: "SALE", saleNote: "" })}
                            className="rounded-md bg-[#8a6b45] px-4 py-2 text-white shadow-sm transition hover:bg-[#6a5841] hover:shadow-md"
                        >
                            Clear
                        </button>
                    </div>
                </form>
                </div>

                <SalesHistory refreshTrigger={refreshTrigger} />
            </div>
        </main>
    );
}
