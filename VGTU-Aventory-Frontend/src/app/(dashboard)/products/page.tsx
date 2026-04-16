"use client";

import { useEffect, useState } from "react";
import { apiUrl } from "@/lib/api/url";
import { fetchWithSession } from "@/features/auth/services/fetchWithSession";

import { Product, ApiProduct, mapApiProduct } from "@/features/dashboard/types/product";
import { ProductForm } from "./ProductForm";

type EditFormState = {
  name: string;
  quantity: string;
  description: string;
  photoUrl: string;
  qrCode: string;
  price: string;
};

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [viewingProduct, setViewingProduct] = useState<Product | null>(null);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [editForm, setEditForm] = useState<EditFormState | null>(null);
  const [savingEdit, setSavingEdit] = useState(false);
  const [addingProduct, setAddingProduct] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError(null);

      const res = await fetchWithSession(apiUrl("/api/products/all"));
      if (!res.ok) throw new Error(`API error: ${res.status}`);

      const data: ApiProduct[] = await res.json();
      setProducts(data.map(mapApiProduct));
      setSelectedIds([]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch products");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchProducts();
  }, []);

  const handleAdd = async (data: Omit<Product, "id">) => {
    try {
      const res = await fetchWithSession(apiUrl("/api/products"), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          productName: data.name,
          price: data.price,
          productDescription: data.description,
          photoUrl: data.photoUrl,
          quantity: data.quantity,
          qrCode: data.qrCode,
        }),
      });

      if (!res.ok) throw new Error(`Failed to add product: ${res.status}`);

      await fetchProducts();
      setAddingProduct(false);
    } catch (err) {
      alert(`Error adding product: ${err instanceof Error ? err.message : "Unknown error"}`);
    }
  };

  const openEditModal = (product: Product) => {
    setError(null);
    setEditingProduct(product);
    setEditForm({
      name: product.name,
      quantity: String(product.quantity),
      description: product.description,
      photoUrl: product.photoUrl,
      qrCode: product.qrCode,
      price: String(product.price),
    });
  };

  const closeEditModal = () => {
    if (savingEdit) {
      return;
    }

    setEditingProduct(null);
    setEditForm(null);
  };

  const handleEditInputChange = (field: keyof EditFormState, value: string) => {
    setEditForm((prev) => {
      if (!prev) {
        return null;
      }

      return {
        ...prev,
        [field]: value,
      };
    });
  };

  const handleEdit = async () => {
    if (!editingProduct || !editForm) {
      return;
    }

    const trimmedName = editForm.name.trim();
    if (!trimmedName) {
      setError("Product name cannot be empty.");
      return;
    }

    const parsedQuantity = Number(editForm.quantity);
    if (!Number.isFinite(parsedQuantity) || Number.isNaN(parsedQuantity)) {
      setError("Quantity must be a valid number.");
      return;
    }

    const parsedPrice = Number(editForm.price);
    if (!Number.isFinite(parsedPrice) || Number.isNaN(parsedPrice)) {
      setError("Price must be a valid number.");
      return;
    }

    setSavingEdit(true);
    setError(null);

    try {
      const res = await fetchWithSession(apiUrl(`/api/products/${editingProduct.id}`), {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          productName: trimmedName,
          price: parsedPrice,
          productDescription: editForm.description,
          photoUrl: editForm.photoUrl,
          quantity: parsedQuantity,
          qrCode: editForm.qrCode,
        }),
      });

      if (!res.ok) {
        throw new Error(`Failed to update product: ${res.status}`);
      }

      await fetchProducts();
      setEditingProduct(null);
      setEditForm(null);
    } catch (err) {
      alert(`Error updating product: ${err instanceof Error ? err.message : "Unknown error"}`);
    } finally {
      setSavingEdit(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Are you sure you want to delete this product?")) return;

    try {
      setIsDeleting(true);

      const res = await fetchWithSession(apiUrl(`/api/products/${id}`), {
        method: "DELETE",
      });

      if (!res.ok) throw new Error(`Failed to delete product: ${res.status}`);

      await fetchProducts();
    } catch (err) {
      alert(`Error deleting product: ${err instanceof Error ? err.message : "Unknown error"}`);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedIds.length === 0) {
      return;
    }

    if (!confirm("Are you sure you want to delete the selected products?")) {
      return;
    }

    try {
      setIsDeleting(true);

      const responses = await Promise.all(
        selectedIds.map((id) =>
          fetchWithSession(apiUrl(`/api/products/${id}`), {
            method: "DELETE",
          }),
        ),
      );

      if (responses.some((res) => !res.ok)) {
        throw new Error("Failed to delete one or more selected products.");
      }

      await fetchProducts();
    } catch (err) {
      alert(`Error deleting products: ${err instanceof Error ? err.message : "Unknown error"}`);
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

    setSelectedIds(products.map((product) => product.id));
  };

  const toggleSelectOne = (id: string) => {
    setSelectedIds((prevSelected) => {
      if (prevSelected.includes(id)) {
        return prevSelected.filter((selectedId) => selectedId !== id);
      }

      return [...prevSelected, id];
    });
  };

  return (
    <main className="min-h-screen bg-transparent p-6">
      <div className="mx-auto max-w-6xl">
        <header className="mb-6 flex items-center justify-between gap-3">
          <div>
            <h1 className="text-3xl font-bold text-[#2d2418]">Products</h1>
            <p className="text-sm text-[#6a5841]">Manage your inventory items</p>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => void handleDeleteSelected()}
              className="rounded-md bg-red-600 px-4 py-2 text-white shadow-sm transition hover:bg-red-700 hover:shadow-md disabled:cursor-not-allowed disabled:opacity-60"
              disabled={selectedIds.length === 0 || loading || isDeleting}
            >
              Delete selected ({selectedIds.length})
            </button>
            <button
              onClick={() => setAddingProduct(true)}
              className="rounded-md bg-[#f59e0b] px-4 py-2 text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md disabled:cursor-not-allowed disabled:opacity-60"
              disabled={loading || isDeleting}
            >
              Add Product
            </button>
          </div>
        </header>

        {error && (
          <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4 text-red-800">
            <strong>Error:</strong> {error}
          </div>
        )}

        {addingProduct && (
          <div className="mb-6 rounded-xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
            <h2 className="mb-4 text-lg font-semibold text-[#2d2418]">Add New Product</h2>
            <ProductForm onSave={handleAdd} onCancel={() => setAddingProduct(false)} />
          </div>
        )}

        <div className="rounded-xl border border-[#f0dfc5] bg-white shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
          <div className="p-6">
            <h2 className="text-lg font-semibold text-[#2d2418]">All Products</h2>
            <p className="mt-1 text-sm text-[#6a5841]">View and manage your inventory</p>
          </div>

          {loading ? (
            <div className="p-6 text-center text-[#8a6b45]">Loading products...</div>
          ) : products.length === 0 ? (
            <div className="p-6 text-center text-[#8a6b45]">No products found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full table-fixed border-collapse text-left text-sm">
                <thead className="border-b border-[#f0dfc5] bg-[#fff4e2]">
                  <tr>
                    <th className="w-[6%] bg-[#fff4e2] px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-[#8a6b45]">
                      <input
                        type="checkbox"
                        checked={allSelected}
                        onChange={toggleSelectAll}
                        aria-label="Select all products"
                      />
                    </th>
                    <th className="w-[24%] bg-[#fff4e2] px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-[#8a6b45]">
                      Name
                    </th>
                    <th className="w-[15%] bg-[#fff4e2] px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-[#8a6b45]">
                      Quantity
                    </th>
                    <th className="w-[15%] bg-[#fff4e2] px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-[#8a6b45]">
                      Price
                    </th>
                    <th className="w-[20%] bg-[#fff4e2] px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-[#8a6b45]">
                      QR Code
                    </th>
                    <th className="w-[20%] bg-[#fff4e2] px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-[#8a6b45]">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#f0dfc5]">
                  {products.map((product) => (
                    <tr
                      key={product.id}
                      className="cursor-pointer hover:bg-[#fffaf3]"
                      onClick={() => setViewingProduct(product)}
                    >
                      <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-[#2d2418]">
                        <input
                          type="checkbox"
                          checked={selectedIds.includes(product.id)}
                          onClick={(event) => event.stopPropagation()}
                          onChange={() => toggleSelectOne(product.id)}
                          aria-label={`Select product ${product.id}`}
                          disabled={isDeleting}
                        />
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-[#2d2418]">
                        {product.name}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-[#6a5841]">
                        {product.quantity}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-[#6a5841]">
                        ${product.price.toFixed(2)}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-[#6a5841]">
                        {product.qrCode}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                        <div className="flex justify-end gap-2">
                          <button
                            onClick={(event) => {
                              event.stopPropagation();
                              openEditModal(product);
                            }}
                            className="rounded-md bg-[#f59e0b] px-4 py-2 text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md"
                          >
                            Edit
                          </button>
                          <button
                            onClick={(event) => {
                              event.stopPropagation();
                              void handleDelete(product.id);
                            }}
                            className="rounded-md bg-[#c2410c] px-4 py-2 text-white shadow-sm transition hover:bg-[#9a3412] hover:shadow-md disabled:cursor-not-allowed disabled:opacity-60"
                            disabled={isDeleting}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {editingProduct && editForm && (
          <div className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/40 px-4 py-6 sm:items-center">
            <div className="w-full max-w-2xl rounded-xl bg-white p-6 shadow-2xl sm:p-7">
              <h2 className="text-xl font-bold text-slate-900">
                Edit Product #{editingProduct.id}
              </h2>

              <div className="mt-4 grid max-h-[70vh] grid-cols-1 gap-4 overflow-y-auto pr-1 sm:grid-cols-2">
                <label className="text-sm font-medium text-slate-700">
                  <span>Name</span>
                  <input
                    type="text"
                    className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-slate-500"
                    value={editForm.name}
                    onChange={(event) => handleEditInputChange("name", event.target.value)}
                    disabled={savingEdit}
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  <span>Quantity</span>
                  <input
                    type="number"
                    step="1"
                    className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-slate-500"
                    value={editForm.quantity}
                    onChange={(event) => handleEditInputChange("quantity", event.target.value)}
                    disabled={savingEdit}
                  />
                </label>

                <label className="text-sm font-medium text-slate-700 sm:col-span-2">
                  <span>Description</span>
                  <textarea
                    className="mt-1 min-h-24 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-slate-500"
                    value={editForm.description}
                    onChange={(event) => handleEditInputChange("description", event.target.value)}
                    disabled={savingEdit}
                  />
                </label>

                <label className="text-sm font-medium text-slate-700 sm:col-span-2">
                  <span>Photo URL</span>
                  <input
                    type="url"
                    className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-slate-500"
                    value={editForm.photoUrl}
                    onChange={(event) => handleEditInputChange("photoUrl", event.target.value)}
                    disabled={savingEdit}
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  <span>QR Code</span>
                  <input
                    type="text"
                    className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-slate-500"
                    value={editForm.qrCode}
                    onChange={(event) => handleEditInputChange("qrCode", event.target.value)}
                    disabled={savingEdit}
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  <span>Price</span>
                  <input
                    type="number"
                    step="0.01"
                    className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-slate-500"
                    value={editForm.price}
                    onChange={(event) => handleEditInputChange("price", event.target.value)}
                    disabled={savingEdit}
                  />
                </label>
              </div>

              <div className="mt-6 flex justify-end gap-2">
                <button
                  type="button"
                  className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
                  onClick={closeEditModal}
                  disabled={savingEdit}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="rounded-md bg-[#f59e0b] px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md disabled:cursor-not-allowed disabled:opacity-60"
                  onClick={() => void handleEdit()}
                  disabled={savingEdit}
                >
                  {savingEdit ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </div>
          </div>
        )}

        {viewingProduct && (
          <div className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/40 px-4 py-6 sm:items-center">
            <div className="w-full max-w-2xl rounded-xl bg-white p-6 shadow-2xl sm:p-7">
              <h2 className="text-xl font-bold text-slate-900">
                Product Overview #{viewingProduct.id}
              </h2>

              <div className="mt-4 grid max-h-[70vh] grid-cols-1 gap-4 overflow-y-auto pr-1 sm:grid-cols-2">
                <label className="text-sm font-medium text-slate-700">
                  <span>Name</span>
                  <input
                    type="text"
                    className="mt-1 w-full rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-slate-700 outline-none"
                    value={viewingProduct.name}
                    readOnly
                    disabled
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  <span>Quantity</span>
                  <input
                    type="number"
                    className="mt-1 w-full rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-slate-700 outline-none"
                    value={viewingProduct.quantity}
                    readOnly
                    disabled
                  />
                </label>

                <label className="text-sm font-medium text-slate-700 sm:col-span-2">
                  <span>Description</span>
                  <textarea
                    className="mt-1 min-h-24 w-full rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-slate-700 outline-none"
                    value={viewingProduct.description}
                    readOnly
                    disabled
                  />
                </label>

                <label className="text-sm font-medium text-slate-700 sm:col-span-2">
                  <span>Photo URL</span>
                  <input
                    type="url"
                    className="mt-1 w-full rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-slate-700 outline-none"
                    value={viewingProduct.photoUrl}
                    readOnly
                    disabled
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  <span>QR Code</span>
                  <input
                    type="text"
                    className="mt-1 w-full rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-slate-700 outline-none"
                    value={viewingProduct.qrCode}
                    readOnly
                    disabled
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  <span>Price</span>
                  <input
                    type="number"
                    step="0.01"
                    className="mt-1 w-full rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-slate-700 outline-none"
                    value={viewingProduct.price}
                    readOnly
                    disabled
                  />
                </label>
              </div>

              <div className="mt-6 flex justify-end">
                <button
                  type="button"
                  className="rounded-md bg-[#f59e0b] px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md"
                  onClick={() => setViewingProduct(null)}
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
