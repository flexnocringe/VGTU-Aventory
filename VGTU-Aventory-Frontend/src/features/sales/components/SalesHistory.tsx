"use client";

import { useEffect, useState } from "react";
import { getSalesHistory, SalesHistoryRecord } from "../services/getSalesHistory";

export function SalesHistory() {
    const [sales, setSales] = useState<SalesHistoryRecord[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const loadSalesHistory = async () => {
            try {
                const data = await getSalesHistory();
                setSales(data);
            } catch (err) {
                setError(err instanceof Error ? err.message : "Failed to load sales history");
            } finally {
                setLoading(false);
            }
        };

        loadSalesHistory();
    }, []);

    const formatDate = (dateString: string) => {
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString("en-US", {
                year: "numeric",
                month: "short",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit",
            });
        } catch {
            return dateString;
        }
    };

    if (loading) {
        return (
            <div className="rounded-xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
                <h2 className="text-lg font-semibold text-[#2d2418]">Sales History</h2>
                <p className="mt-2 text-center text-[#8a6b45]">Loading sales history...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-800">
                <strong>Error loading sales history:</strong> {error}
            </div>
        );
    }

    if (sales.length === 0) {
        return (
            <div className="rounded-xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
                <h2 className="text-lg font-semibold text-[#2d2418]">Sales History</h2>
                <p className="mt-2 text-center text-[#8a6b45]">No sales recorded yet</p>
            </div>
        );
    }

    return (
        <div className="rounded-xl border border-[#f0dfc5] bg-white shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
            <div className="border-b border-[#f0dfc5] p-6">
                <h2 className="text-lg font-semibold text-[#2d2418]">Sales History</h2>
                <p className="mt-1 text-sm text-[#6a5841]">Recent sales transactions</p>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full">
                    <thead className="border-b border-[#f0dfc5] bg-[#faf8f5]">
                        <tr>
                            <th className="px-6 py-3 text-left text-sm font-semibold text-[#2d2418]">Product</th>
                            <th className="px-6 py-3 text-left text-sm font-semibold text-[#2d2418]">Quantity</th>
                            <th className="px-6 py-3 text-left text-sm font-semibold text-[#2d2418]">Type</th>
                            <th className="px-6 py-3 text-left text-sm font-semibold text-[#2d2418]">Location</th>
                            <th className="px-6 py-3 text-left text-sm font-semibold text-[#2d2418]">Total Price</th>
                            <th className="px-6 py-3 text-left text-sm font-semibold text-[#2d2418]">Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {sales.map((sale) => (
                            <tr key={sale.id} className="border-b border-[#f0dfc5] transition hover:bg-[#faf8f5]">
                                <td className="px-6 py-4">
                                    <div className="text-sm font-medium text-[#2d2418]">{sale.product.productName}</div>
                                    <div className="text-xs text-[#8a6b45]">Price: ${sale.saleProductPrice.toFixed(2)}</div>
                                </td>
                                <td className="px-6 py-4 text-sm text-[#2d2418]">{sale.quantity}</td>
                                <td className="px-6 py-4">
                                    <span
                                        className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ${
                                            sale.saleType === "SALE"
                                                ? "bg-green-100 text-green-800"
                                                : "bg-blue-100 text-blue-800"
                                        }`}
                                    >
                                        {sale.saleType}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-sm text-[#2d2418]">{sale.saleLocation}</td>
                                <td className="px-6 py-4 text-sm font-semibold text-[#2d2418]">
                                    ${sale.totalPrice.toFixed(2)}
                                </td>
                                <td className="px-6 py-4 text-sm text-[#8a6b45]">{formatDate(sale.saleDate)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
