"use client";

import { useEffect, useMemo, useState } from "react";

type ProductValue =
  | string
  | number
  | boolean
  | null
  | Record<string, unknown>
  | unknown[];

type Product = {
  productId: number;
  [key: string]: ProductValue;
};

const GET_PRODUCTS_URL = "/api/backend/getAllProducts";
const DELETE_PRODUCTS_URL = "/api/backend/deleteProducts";

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const columns = useMemo(() => {
    const allKeys = new Set<string>();
    products.forEach((product) => {
      Object.keys(product).forEach((key) => {
        if (key !== "productId") {
          allKeys.add(key);
        }
      });
    });

    return ["productId", ...Array.from(allKeys)];
  }, [products]);

  useEffect(() => {
    void fetchProducts();
  }, []);

  const fetchProducts = async () => {
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const response = await fetch(GET_PRODUCTS_URL, {
        method: "GET",
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch products: ${response.status}`);
      }

      const data = (await response.json()) as Product[];
      setProducts(data);
      setSelectedIds([]);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not load products.");
    } finally {
      setIsLoading(false);
    }
  };

  const deleteProductsByIds = async (ids: number[]) => {
    if (ids.length === 0) {
      return;
    }

    setIsDeleting(true);
    setErrorMessage(null);

    try {
      const response = await fetch(DELETE_PRODUCTS_URL, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ productId: ids }),
      });

      if (!response.ok) {
        throw new Error(`Failed to delete products: ${response.status}`);
      }

      await fetchProducts();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not delete products.");
    } finally {
      setIsDeleting(false);
    }
  };

  const allSelected = products.length > 0 && selectedIds.length === products.length;

  const toggleSelectAll = () => {
    if (allSelected) {
      setSelectedIds([]);
      return;
    }

    setSelectedIds(products.map((product) => product.productId));
  };

  const toggleSelectOne = (id: number) => {
    setSelectedIds((prevSelected) => {
      if (prevSelected.includes(id)) {
        return prevSelected.filter((selectedId) => selectedId !== id);
      }

      return [...prevSelected, id];
    });
  };

  const renderCellValue = (value: ProductValue) => {
    if (value === null || value === undefined) {
      return "-";
    }

    if (Array.isArray(value) || typeof value === "object") {
      return JSON.stringify(value);
    }

    return String(value);
  };

  return (
    <main className="min-h-screen bg-slate-100 px-4 py-10 text-slate-900">
      <div className="mx-auto w-full max-w-6xl rounded-2xl bg-white p-6 shadow-lg">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-bold">Products</h1>
          <div className="flex items-center gap-2">
            <button
              type="button"
              className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
              onClick={() => void fetchProducts()}
              disabled={isLoading || isDeleting}
            >
              Refresh
            </button>
            <button
              type="button"
              className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
              onClick={() => void deleteProductsByIds(selectedIds)}
              disabled={selectedIds.length === 0 || isDeleting || isLoading}
            >
              Delete selected ({selectedIds.length})
            </button>
          </div>
        </div>

        {errorMessage && (
          <p className="mb-4 rounded-md border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
            {errorMessage}
          </p>
        )}

        {isLoading ? (
          <p className="py-6 text-sm text-slate-600">Loading products...</p>
        ) : products.length === 0 ? (
          <p className="py-6 text-sm text-slate-600">No products found.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full border-collapse text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50">
                  <th className="px-3 py-3">
                    <input
                      type="checkbox"
                      checked={allSelected}
                      onChange={toggleSelectAll}
                      aria-label="Select all products"
                    />
                  </th>
                  {columns.map((column) => (
                    <th key={column} className="px-3 py-3 font-semibold capitalize">
                      {column}
                    </th>
                  ))}
                  <th className="px-3 py-3 font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.productId} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="px-3 py-3 align-middle">
                      <input
                        type="checkbox"
                        checked={selectedIds.includes(product.productId)}
                        onChange={() => toggleSelectOne(product.productId)}
                        aria-label={`Select product ${product.productId}`}
                      />
                    </td>
                    {columns.map((column) => {
                      const value = product[column as keyof Product];
                      return (
                        <td key={`${product.productId}-${column}`} className="px-3 py-3 align-middle">
                          {renderCellValue(value)}
                        </td>
                      );
                    })}
                    <td className="px-3 py-3 align-middle">
                      <button
                        type="button"
                        className="rounded-md bg-red-500 px-3 py-1.5 font-medium text-white hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-60"
                        onClick={() => void deleteProductsByIds([product.productId])}
                        disabled={isDeleting || isLoading}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </main>
  );
}
