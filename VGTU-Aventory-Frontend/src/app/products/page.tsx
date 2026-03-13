"use client";

import { useEffect, useState } from "react";

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
  price: number;
};

function mapApiProduct(p: ApiProduct): Product {
  return {
    id: String(p.productId),
    name: p.productName,
    quantity: p.quantity,
    description: p.productDescription,
    photoUrl: p.photoUrl,
    qrCode: p.qrCode,
    price: p.price,
  };
}

function ProductForm({
  product,
  onSave,
  onCancel,
}: {
  product?: Product;
  onSave: (data: Omit<Product, "id">) => void;
  onCancel: () => void;
}) {
  const [form, setForm] = useState({
    name: product?.name || "",
    quantity: product?.quantity || 0,
    description: product?.description || "",
    photoUrl: product?.photoUrl || "",
    qrCode: product?.qrCode || "",
    price: product?.price || 0,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(form);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700">Name</label>
        <input
          type="text"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700">Quantity</label>
        <input
          type="number"
          value={form.quantity}
          onChange={(e) => setForm({ ...form, quantity: Number(e.target.value) })}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700">Price</label>
        <input
          type="number"
          step="0.01"
          value={form.price}
          onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700">Description</label>
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700">Photo URL</label>
        <input
          type="url"
          value={form.photoUrl}
          onChange={(e) => setForm({ ...form, photoUrl: e.target.value })}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700">QR Code</label>
        <input
          type="text"
          value={form.qrCode}
          onChange={(e) => setForm({ ...form, qrCode: e.target.value })}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        />
      </div>
      <div className="flex gap-2">
        <button
          type="submit"
          className="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
        >
          {product ? "Update" : "Add"} Product
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md bg-gray-600 px-4 py-2 text-white hover:bg-gray-700"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [addingProduct, setAddingProduct] = useState(false);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await fetch("http://localhost:8080/api/products/all");
      if (!res.ok) throw new Error(`API error: ${res.status}`);
      const data: ApiProduct[] = await res.json();
      setProducts(data.map(mapApiProduct));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch products");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleAdd = async (data: Omit<Product, "id">) => {
    try {
      const res = await fetch("http://localhost:8080/api/products", {
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

  const handleEdit = async (data: Omit<Product, "id">) => {
    if (!editingProduct) return;
    try {
      const res = await fetch(`http://localhost:8080/api/products/${editingProduct.id}`, {
        method: "PUT",
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
      if (!res.ok) throw new Error(`Failed to update product: ${res.status}`);
      await fetchProducts();
      setEditingProduct(null);
    } catch (err) {
      alert(`Error updating product: ${err instanceof Error ? err.message : "Unknown error"}`);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Are you sure you want to delete this product?")) return;
    try {
      const res = await fetch(`http://localhost:8080/api/products/${id}`, {
        method: "DELETE",
      });
      if (!res.ok) throw new Error(`Failed to delete product: ${res.status}`);
      await fetchProducts();
    } catch (err) {
      alert(`Error deleting product: ${err instanceof Error ? err.message : "Unknown error"}`);
    }
  };

  return (
    <main className="min-h-screen bg-slate-50 p-6">
      <div className="mx-auto max-w-6xl">
        <header className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Products</h1>
            <p className="text-sm text-slate-600">Manage your inventory items</p>
          </div>
          <button
            onClick={() => setAddingProduct(true)}
            className="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
          >
            Add Product
          </button>
        </header>

        {error && (
          <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4 text-red-800">
            <strong>Error:</strong> {error}
          </div>
        )}

        {addingProduct && (
          <div className="mb-6 rounded-xl border border-gray-200 bg-white p-6">
            <h2 className="mb-4 text-lg font-semibold">Add New Product</h2>
            <ProductForm onSave={handleAdd} onCancel={() => setAddingProduct(false)} />
          </div>
        )}

        {editingProduct && (
          <div className="mb-6 rounded-xl border border-gray-200 bg-white p-6">
            <h2 className="mb-4 text-lg font-semibold">Edit Product</h2>
            <ProductForm
              product={editingProduct}
              onSave={handleEdit}
              onCancel={() => setEditingProduct(null)}
            />
          </div>
        )}

        <div className="rounded-xl border border-gray-200 bg-white shadow-sm">
          <div className="p-6">
            <h2 className="text-lg font-semibold text-gray-900">All Products</h2>
            <p className="mt-1 text-sm text-gray-600">View and manage your inventory</p>
          </div>

          {loading ? (
            <div className="p-6 text-center text-gray-500">Loading products...</div>
          ) : products.length === 0 ? (
            <div className="p-6 text-center text-gray-500">No products found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="border-b border-gray-200 bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      Name
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      Quantity
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      Price
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      QR Code
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {products.map((product) => (
                    <tr key={product.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {product.name}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {product.quantity}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        ${product.price.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {product.qrCode}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <button
                          onClick={() => setEditingProduct(product)}
                          className="mr-2 text-blue-600 hover:text-blue-900"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDelete(product.id)}
                          className="text-red-600 hover:text-red-900"
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
      </div>
    </main>
  );
}
