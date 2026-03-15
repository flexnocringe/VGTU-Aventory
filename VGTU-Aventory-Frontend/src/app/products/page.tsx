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
const UPDATE_PRODUCT_URL = "/api/backend/editProduct";
const INLINE_EDITABLE_FIELDS = new Set(["productName", "productDescription", "price", "quantity"]);

type InlineEditState = {
  productId: number;
  field: string;
  value: string;
};

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [editForm, setEditForm] = useState<Record<string, string>>({});
  const [inlineEdit, setInlineEdit] = useState<InlineEditState | null>(null);

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
      const response = await fetch(GET_PRODUCTS_URL);

      if (!response.ok) {
        throw new Error(`Failed to fetch products: ${response.status}`);
      }

      const data = (await response.json()) as Product[];
      setProducts(data);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not load products.");
    } finally {
      setIsLoading(false);
    }
  };

  const toEditableString = (value: unknown) => {
    if (value === null || value === undefined) {
      return "";
    }

    if (Array.isArray(value) || typeof value === "object") {
      return JSON.stringify(value);
    }

    return String(value);
  };

  const parseEditedValue = (
    field: string,
    rawValue: string,
    previousValue: ProductValue | undefined,
  ): { ok: true; value: ProductValue } | { ok: false; error: string } => {
    const trimmed = rawValue.trim();

    if (field === "productName") {
      if (!trimmed) {
        return { ok: false, error: "Product name cannot be empty." };
      }
      return { ok: true, value: trimmed };
    }

    if (field === "productId") {
      const parsed = Number(trimmed);
      if (!Number.isFinite(parsed) || Number.isNaN(parsed)) {
        return { ok: false, error: "Product ID must be a valid number." };
      }
      return { ok: true, value: Math.trunc(parsed) };
    }

    if (field === "price") {
      if (!trimmed) {
        return { ok: true, value: null };
      }

      const parsed = Number(trimmed);
      if (!Number.isFinite(parsed) || Number.isNaN(parsed)) {
        return { ok: false, error: "Price must be a valid number." };
      }
      return { ok: true, value: parsed };
    }

    if (field === "quantity") {
      if (!trimmed) {
        return { ok: true, value: 0 };
      }

      const parsed = Number(trimmed);
      if (!Number.isFinite(parsed) || Number.isNaN(parsed)) {
        return { ok: false, error: "Quantity must be a valid number." };
      }
      return { ok: true, value: Math.max(0, Math.trunc(parsed)) };
    }

    if (Array.isArray(previousValue) || typeof previousValue === "object") {
      if (!trimmed) {
        return { ok: true, value: null };
      }

      try {
        return { ok: true, value: JSON.parse(trimmed) as ProductValue };
      } catch {
        return { ok: false, error: `Field ${field} must contain valid JSON.` };
      }
    }

    if (typeof previousValue === "boolean") {
      if (trimmed.toLowerCase() === "true") {
        return { ok: true, value: true };
      }
      if (trimmed.toLowerCase() === "false") {
        return { ok: true, value: false };
      }
      return { ok: false, error: `Field ${field} must be true or false.` };
    }

    if (typeof previousValue === "number") {
      if (!trimmed) {
        return { ok: true, value: null };
      }

      const parsed = Number(trimmed);
      if (!Number.isFinite(parsed) || Number.isNaN(parsed)) {
        return { ok: false, error: `Field ${field} must be a valid number.` };
      }
      return { ok: true, value: parsed };
    }

    if (!trimmed) {
      return { ok: true, value: null };
    }

    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
      try {
        return { ok: true, value: JSON.parse(trimmed) as ProductValue };
      } catch {
        return { ok: false, error: `Field ${field} must contain valid JSON.` };
      }
    }

    return { ok: true, value: trimmed };
  };

  const saveUpdatedProduct = async (product: Product) => {
    setIsSaving(true);
    setErrorMessage(null);

    try {
      const response = await fetch(UPDATE_PRODUCT_URL, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(product),
      });

      if (!response.ok) {
        throw new Error(`Failed to update product: ${response.status}`);
      }

      await fetchProducts();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not update product.");
    } finally {
      setIsSaving(false);
    }
  };

  const openEditModal = (product: Product) => {
    setEditingProduct(product);
    const initialForm: Record<string, string> = {};
    Object.keys(product).forEach((field) => {
      initialForm[field] = toEditableString(product[field]);
    });
    setEditForm(initialForm);
  };

  const closeEditModal = () => {
    if (isSaving) {
      return;
    }

    setEditingProduct(null);
  };

  const handleEditInputChange = (field: string, value: string) => {
    setEditForm((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const saveProductEdit = async () => {
    if (!editingProduct) {
      return;
    }

    const nextProduct: Product = { ...editingProduct };
    for (const field of Object.keys(editingProduct)) {
      const parsed = parseEditedValue(field, editForm[field] ?? "", editingProduct[field]);
      if (!parsed.ok) {
        setErrorMessage(parsed.error);
        return;
      }
      nextProduct[field] = parsed.value;
    }

    setEditingProduct(null);
    await saveUpdatedProduct(nextProduct);
  };

  const beginInlineEdit = (product: Product, field: string) => {
    if (!INLINE_EDITABLE_FIELDS.has(field) || isSaving || isLoading) {
      return;
    }

    setInlineEdit({
      productId: product.productId,
      field,
      value: toEditableString(product[field]),
    });
  };

  const changeInlineEditValue = (value: string) => {
    setInlineEdit((prev) => {
      if (!prev) {
        return null;
      }

      return {
        ...prev,
        value,
      };
    });
  };

  const commitInlineEdit = async (product: Product, field: string) => {
    if (!inlineEdit) {
      return;
    }

    if (inlineEdit.productId !== product.productId || inlineEdit.field !== field) {
      return;
    }

    const parsed = parseEditedValue(field, inlineEdit.value, product[field]);
    if (!parsed.ok) {
      setErrorMessage(parsed.error);
      return;
    }

    const updatedProduct: Product = {
      ...product,
      [field]: parsed.value,
    };

    setInlineEdit(null);
    await saveUpdatedProduct(updatedProduct);
  };

  const cancelInlineEdit = () => {
    setInlineEdit(null);
  };

  const renderCellValue = (value: ProductValue | undefined) => {
    if (value === null || value === undefined) return "-";
    if (Array.isArray(value) || typeof value === "object") return JSON.stringify(value);
    return String(value);
  };

  return (
    <main className="min-h-screen bg-slate-100 px-4 py-10 text-slate-900">
      <div className="mx-auto w-full max-w-6xl rounded-2xl bg-white p-6 shadow-lg">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-bold">Products</h1>
          <button
            type="button"
            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
            onClick={() => void fetchProducts()}
            disabled={isLoading}
          >
            Refresh
          </button>
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
                  <tr
                    key={product.productId}
                    className="border-b border-slate-100 hover:bg-slate-50"
                  >
                    {columns.map((column) => {
                      const isInlineEditing =
                        inlineEdit?.productId === product.productId && inlineEdit.field === column;
                      const cellIsEditable = INLINE_EDITABLE_FIELDS.has(column);

                      return (
                        <td
                          key={`${product.productId}-${column}`}
                          className={`px-3 py-3 align-middle ${
                            cellIsEditable ? "cursor-text" : ""
                          }`}
                          onDoubleClick={() => beginInlineEdit(product, column)}
                          title={
                            cellIsEditable ? "Double click to edit this field inline" : undefined
                          }
                        >
                          {isInlineEditing ? (
                            <input
                              autoFocus
                              type={column === "price" || column === "quantity" ? "number" : "text"}
                              step={column === "price" ? "0.01" : "1"}
                              className="w-full rounded-md border border-blue-400 px-2 py-1 outline-none"
                              value={inlineEdit.value}
                              onChange={(event) => changeInlineEditValue(event.target.value)}
                              onBlur={() => {
                                void commitInlineEdit(product, column);
                              }}
                              onKeyDown={(event) => {
                                if (event.key === "Enter") {
                                  event.preventDefault();
                                  void commitInlineEdit(product, column);
                                }

                                if (event.key === "Escape") {
                                  event.preventDefault();
                                  cancelInlineEdit();
                                }
                              }}
                              disabled={isSaving}
                            />
                          ) : (
                            renderCellValue(product[column])
                          )}
                        </td>
                      );
                    })}
                    <td className="px-3 py-3 align-middle">
                      <button
                        type="button"
                        className="rounded-md bg-blue-600 px-3 py-1.5 font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                        onClick={() => openEditModal(product)}
                        disabled={isLoading || isSaving}
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {editingProduct && (
          <div className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/40 px-4 py-6 sm:items-center">
            <div className="w-full max-w-6xl rounded-xl bg-white p-6 shadow-2xl sm:p-7">
              <h2 className="text-xl font-bold text-slate-900">
                Edit Product #{editingProduct.productId}
              </h2>

              <div className="mt-4 grid max-h-[70vh] grid-cols-1 gap-4 overflow-y-auto pr-1 sm:grid-cols-2 xl:grid-cols-3">
                {Object.keys(editingProduct).map((field) => {
                  const value = editingProduct[field];
                  const isJsonField = Array.isArray(value) || typeof value === "object";
                  const isBooleanField = typeof value === "boolean";
                  const isNumberField = typeof value === "number" || field === "price" || field === "quantity";

                  return (
                    <label
                      key={field}
                      className={`text-sm font-medium text-slate-700 ${
                        isJsonField ? "sm:col-span-2 xl:col-span-3" : ""
                      }`}
                    >
                      <span className="capitalize">{field}</span>

                      {isJsonField ? (
                        <textarea
                          className="mt-1 min-h-24 w-full rounded-md border border-slate-300 px-3 py-2 font-mono text-sm text-slate-900 outline-none focus:border-slate-500"
                          value={editForm[field] ?? ""}
                          onChange={(event) => handleEditInputChange(field, event.target.value)}
                          disabled={isSaving}
                        />
                      ) : isBooleanField ? (
                        <select
                          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-slate-500"
                          value={editForm[field] ?? "false"}
                          onChange={(event) => handleEditInputChange(field, event.target.value)}
                          disabled={isSaving}
                        >
                          <option value="true">true</option>
                          <option value="false">false</option>
                        </select>
                      ) : (
                        <input
                          type={isNumberField ? "number" : "text"}
                          step={field === "price" ? "0.01" : "1"}
                          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-slate-500"
                          value={editForm[field] ?? ""}
                          onChange={(event) => handleEditInputChange(field, event.target.value)}
                          disabled={isSaving}
                        />
                      )}
                    </label>
                  );
                })}
              </div>

              <div className="mt-6 flex justify-end gap-2">
                <button
                  type="button"
                  className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
                  onClick={closeEditModal}
                  disabled={isSaving}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                  onClick={() => void saveProductEdit()}
                  disabled={isSaving}
                >
                  {isSaving ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
