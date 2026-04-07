"use client";

import { useEffect, useMemo, useState } from "react";

type ApiProduct = {
  productId: number;
  productName: string;
  owner: string | null;
  sales: unknown[];
  price: number;
  productDescription: string;
  photoUrl: string;
  quantity: number;
  qrCode: string;
};

type Product = {
  id: string;
  name: string;
  quantity: number;
  description: string;
  photoUrl: string;
  qrCode: string;
};

function mapApiProduct(p: ApiProduct): Product {
  return {
    id: String(p.productId),
    name: p.productName,
    quantity: p.quantity,
    description: p.productDescription,
    photoUrl: p.photoUrl,
    qrCode: p.qrCode,
  };
}

function SummaryCard({ title, value, description }: { title: string; value: string | number; description: string }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
      <div className="text-sm font-medium text-gray-500">{title}</div>
      <div className="mt-2 text-3xl font-semibold text-gray-900">{value}</div>
      <div className="mt-1 text-sm text-gray-600">{description}</div>
    </div>
  );
}

function AnalyticsPanel() {
  const weeklyInbound = [60, 80, 72, 90, 45, 70, 55];
  const maxValue = Math.max(...weeklyInbound);

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-900">Analytics</h2>
          <p className="mt-1 text-sm text-gray-600">Inventory movements & trends (placeholder)</p>
        </div>
        <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700">Live</span>
      </div>

      <div className="mt-6">
        <div className="flex items-end justify-between gap-2">
          {weeklyInbound.map((value, idx) => {
            const height = Math.round((value / maxValue) * 100);
            return (
              <div key={idx} className="flex-1">
                <div className="h-24 w-full rounded-lg bg-gray-100">
                  <div
                    className="h-full w-full rounded-lg bg-gradient-to-t from-blue-600 to-blue-300"
                    style={{ height: `${height}%` }}
                  />
                </div>
                <div className="mt-2 text-center text-xs text-gray-500">Day {idx + 1}</div>
              </div>
            );
          })}
        </div>
        <div className="mt-6 flex items-center justify-between text-sm text-gray-600">
          <span>Incoming items</span>
          <span className="font-semibold text-gray-900">{weeklyInbound.reduce((a, b) => a + b, 0)}</span>
        </div>
      </div>
    </div>
  );
}

export default function Home() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    const url = "http://localhost:8080/api/products/all";

    async function loadProducts() {
      try {
        setLoading(true);
        setError(null);

        const res = await fetch(url, { signal: controller.signal });
        if (!res.ok) {
          throw new Error(`API returned ${res.status} ${res.statusText}`);
        }

        const data = (await res.json()) as ApiProduct[];
        setProducts(data.map(mapApiProduct));
      } catch (err) {
        if (err instanceof Error && err.name === "AbortError") return;
        setError(err instanceof Error ? err.message : "Unknown error");
      } finally {
        setLoading(false);
      }
    }

    loadProducts();
    return () => controller.abort();
  }, []);

  const summary = useMemo(() => {
    const totalProducts = products.length;
    const totalStock = products.reduce((sum, p) => sum + p.quantity, 0);
    const lowStockThreshold = 20;
    const lowStockCount = products.filter((p) => p.quantity <= lowStockThreshold).length;
    const categories = 0;

    return { totalProducts, totalStock, lowStockCount, categories };
  }, [products]);

  return (
    <main className="min-h-screen bg-slate-50 p-6">
      <div className="mx-auto flex max-w-6xl flex-col gap-6">
        <header className="flex flex-col gap-2">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-slate-900">Inventory Dashboard</h1>
              <p className="text-sm text-slate-600">
                Overview of current stock levels and analytics for your inventory management.
              </p>
            </div>
            <a
              href="/products"
              className="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
            >
              View All Products
            </a>
          </div>
        </header>

        {error ? (
          <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-800">
            <strong>Unable to load products:</strong> {error}
          </div>
        ) : (
          <>
            <section className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
              <SummaryCard
                title="Total products"
                value={loading ? "—" : summary.totalProducts}
                description="How many unique SKUs are in the system"
              />
              <SummaryCard
                title="Total stock"
                value={loading ? "—" : summary.totalStock}
                description="Total items currently in inventory"
              />
              <SummaryCard
                title="Low stock items"
                value={loading ? "—" : summary.lowStockCount}
                description="Products at or below the low stock threshold(or smt else)"
              />
              <SummaryCard
                title="Categories"
                value={"—"}
                description="(category tracking not available yet)"
              />
            </section>

            <section className="grid gap-6 lg:grid-cols-3">
              <div className="lg:col-span-2">
                <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
                  <div className="flex items-start justify-between">
                    <div>
                      <h2 className="text-lg font-semibold text-gray-900">Something important</h2>
                      <p className="mt-1 text-sm text-gray-600">Something too important</p>
                    </div>
                    <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700">LIVE YAY</span>
                  </div>

                  <div className="mt-6">
                    <div className="flex items-end justify-between gap-2">
                      something way more more important
                    </div>
                  </div>
                </div>
              </div>
              <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900">Product snapshots</h2>
                    <p className="mt-1 text-sm text-gray-600">Quick look at the most recent items.</p>
                  </div>
                  {loading && <span className="text-sm font-medium text-slate-500">Loading…</span>}
                </div>

                <div className="mt-5 space-y-3">
                  {loading ? (
                    <div className="text-sm text-slate-500">Fetching products from API…</div>
                  ) : products.length === 0 ? (
                    <div className="text-sm text-slate-500">No products returned from the API.</div>
                  ) : (
                    products.slice(0, 4).map((product) => (
                      <div
                        key={product.id}
                        className="flex items-center justify-between rounded-lg border border-gray-100 bg-slate-50 px-4 py-3"
                      >
                        <div>
                          <div className="font-medium text-slate-900">{product.name}</div>
                          <div className="text-xs text-slate-500">{product.description}</div>
                        </div>
                        <div className="text-right">
                          <div className="text-sm font-semibold text-slate-900">{product.quantity}</div>
                          <div className="text-xs text-slate-500">{product.qrCode}</div>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </section>
          </>
        )}
      </div>
    </main>
  );
}
