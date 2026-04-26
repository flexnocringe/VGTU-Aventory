"use client";

import { useEffect, useState } from "react";
import { useAnalyticsDates } from "@/features/analytics/context/AnalyticsDatesContext";
import { getProductSalesComparison } from "@/features/analytics/services/getProductSalesComparison";
import { getAllProducts } from "@/features/dashboard/services/getAllProducts";
import type { ProductSalesComparison } from "@/features/analytics/types/analytics";
import type { Product } from "@/features/dashboard/types/product";

export function ProductSalesComparison() {
  const { dateRange, error: dateError } = useAnalyticsDates();
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const [selectedProductIds, setSelectedProductIds] = useState<number[]>([]);
  const [comparisonData, setComparisonData] = useState<ProductSalesComparison[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const selectedProducts = allProducts
    .filter((product) => selectedProductIds.includes(Number(product.id)))
    .map((product) => ({ id: Number(product.id), name: product.name }));

  useEffect(() => {
    async function loadProducts() {
      try {
        const products = await getAllProducts();
        setAllProducts(products);
      } catch (requestError) {
        console.error("Failed to load products:", requestError);
      }
    }

    loadProducts();
  }, []);

  useEffect(() => {
    if (!dateRange.startDate || !dateRange.endDate || selectedProductIds.length === 0) {
      setComparisonData([]);
      return;
    }

    if (dateRange.startDate > dateRange.endDate) {
      return;
    }

    async function loadComparison() {
      setIsLoading(true);
      try {
        const data = await getProductSalesComparison({
          startDate: dateRange.startDate,
          endDate: dateRange.endDate,
          productIds: selectedProductIds,
        });
        setComparisonData(data);
        setError(null);
      } catch (requestError) {
        const message =
          requestError instanceof Error
            ? requestError.message
            : "Something went wrong while loading product comparison.";
        setError(message);
        setComparisonData([]);
      } finally {
        setIsLoading(false);
      }
    }

    loadComparison();
  }, [dateRange, selectedProductIds]);

  const handleProductSelection = (productId: number) => {
    if (selectedProductIds.includes(productId)) {
      setSelectedProductIds(selectedProductIds.filter((id) => id !== productId));
      return;
    }

    if (selectedProductIds.length < 5) {
      setSelectedProductIds([...selectedProductIds, productId]);
    }
  };

  const handleDropdownSelection = (value: string) => {
    if (!value) {
      return;
    }

    const parsedProductId = Number(value);
    if (!Number.isFinite(parsedProductId)) {
      return;
    }

    handleProductSelection(parsedProductId);
  };

  const maxSalesCount = Math.max(...comparisonData.map((item) => item.totalSalesCount), 1);

  return (
    <section className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)] sm:p-8">
      <h2 className="text-2xl font-semibold text-[#2d2418]">Product sales comparison</h2>
      <p className="mt-2 text-sm text-[#6a5841]">
        Compare total sales count across products for selected date range (max 5).
      </p>

      <div className="mt-6">
        <label className="block text-sm font-medium text-[#5b4a37]">
          Select products ({selectedProductIds.length}/5)
        </label>
        <select
          className="mt-3 w-full rounded-lg border border-[#e9dfcc] bg-white px-3 py-2 text-[#2d2418] outline-none ring-offset-2 transition focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
          defaultValue=""
          onChange={(event) => {
            handleDropdownSelection(event.target.value);
            event.currentTarget.value = "";
          }}
          disabled={allProducts.length === 0}
        >
          <option value="">Choose a product to add</option>
          {allProducts.map((product) => {
            const productId = Number(product.id);
            const isSelected = selectedProductIds.includes(productId);
            const isDisabled = !isSelected && selectedProductIds.length >= 5;

            return (
              <option key={product.id} value={productId} disabled={isDisabled}>
                {product.name}{isSelected ? " (selected)" : ""}
              </option>
            );
          })}
        </select>

        {selectedProducts.length > 0 ? (
          <div className="mt-3 flex flex-wrap gap-2">
            {selectedProducts.map((product) => (
              <button
                key={product.id}
                type="button"
                onClick={() => handleProductSelection(product.id)}
                className="rounded-full border border-[#e7cfad] bg-[#fff4e2] px-3 py-1 text-xs font-medium text-[#5b4a37] transition hover:border-[#d9b47d]"
              >
                {product.name} x
              </button>
            ))}
          </div>
        ) : null}

        {selectedProductIds.length >= 5 ? (
          <p className="mt-2 text-xs text-[#8a6b45]">Maximum 5 products can be compared.</p>
        ) : null}
      </div>

      <div className="mt-6 rounded-xl bg-[#fff4e2] p-4">
        <p className="text-sm text-[#8a6b45]">Results</p>

        {dateError ? (
          <p className="mt-2 text-sm text-red-600">{dateError}</p>
        ) : error ? (
          <p className="mt-2 text-sm text-red-600">{error}</p>
        ) : isLoading ? (
          <p className="mt-2 text-sm text-[#8a6b45]">Loading...</p>
        ) : comparisonData.length === 0 ? (
          <p className="mt-2 text-sm text-[#6a5841]">Select products to compare.</p>
        ) : (
          <div className="mt-4 overflow-x-auto">
            <div className="flex min-h-64 min-w-[28rem] items-end gap-4 rounded-xl border border-[#f2e5d1] bg-white p-4">
              {comparisonData.map((item) => {
                const height = Math.max(Math.round((item.totalSalesCount / maxSalesCount) * 100), 6);
                return (
                  <div key={item.productId} className="flex flex-1 flex-col items-center gap-2">
                    <span className="text-xs font-semibold text-[#9a6b2f]">{item.totalSalesCount}</span>
                    <div className="flex h-44 w-full max-w-16 items-end overflow-hidden rounded-md border border-[#ecdcbf] bg-[#fff8ed]">
                      <div
                        className="w-full rounded-md bg-gradient-to-t from-[#f59e0b] to-[#9a6b2f] transition-all"
                        style={{ height: `${height}%` }}
                      />
                    </div>
                    <span className="w-full truncate text-center text-xs font-medium text-[#2d2418]" title={item.productName}>
                      {item.productName}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
