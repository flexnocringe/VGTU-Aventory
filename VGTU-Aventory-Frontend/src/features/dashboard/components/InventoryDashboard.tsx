"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { AnalyticsProfitCalculator } from "@/features/analytics/components/AnalyticsProfitCalculator";
import { DashboardSummaryCards } from "@/features/dashboard/components/DashboardSummaryCards";
import { InventoryMovementPanel } from "@/features/dashboard/components/InventoryMovementPanel";
import { ProductSnapshots } from "@/features/dashboard/components/ProductSnapshots";
import { getAllProducts } from "@/features/dashboard/services/getAllProducts";
import { Product } from "@/features/dashboard/types/product";

export function InventoryDashboard() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadProducts() {
      try {
        setLoading(true);
        setError(null);
        const data = await getAllProducts(controller.signal);
        setProducts(data);
      } catch (requestError) {
        if (requestError instanceof Error && requestError.name === "AbortError") {
          return;
        }

        setError(requestError instanceof Error ? requestError.message : "Unknown error");
      } finally {
        setLoading(false);
      }
    }

    loadProducts();

    return () => controller.abort();
  }, []);

  const summary = useMemo(() => {
    const totalProducts = products.length;
    const totalStock = products.reduce((sum, product) => sum + product.quantity, 0);
    const lowStockThreshold = 20;
    const lowStockCount = products.filter((product) => product.quantity <= lowStockThreshold).length;
    const categories = 0;

    return { totalProducts, totalStock, lowStockCount, categories };
  }, [products]);

  return (
    <section className="mx-auto flex w-full max-w-6xl flex-col gap-6">
      <header className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-[#2d2418]">Inventory Dashboard</h1>
          <p className="text-sm text-[#6a5841]">
            Overview of stock levels, product snapshots, and profit analytics.
          </p>
        </div>

        <Link
          href="/products"
          className="rounded-lg bg-[#f59e0b] px-4 py-2 text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md"
        >
          View All Products
        </Link>
      </header>

      {error ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-800">
          <strong>Unable to load products:</strong> {error}
        </div>
      ) : (
        <>
          <DashboardSummaryCards summary={summary} loading={loading} />

          <section className="grid gap-6 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <InventoryMovementPanel products={products} loading={loading} />
            </div>
            <ProductSnapshots products={products} loading={loading} />
          </section>
        </>
      )}

      <AnalyticsProfitCalculator />
    </section>
  );
}