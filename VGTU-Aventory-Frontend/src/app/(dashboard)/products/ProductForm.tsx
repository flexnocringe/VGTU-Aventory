import { useState } from "react";
import { QRCodeCanvas } from "qrcode.react";
import { Product } from "@/features/dashboard/types/product";

type CategoryOption = {
  categoryId: number;
  categoryName: string;
};

interface ProductFormProps {
  product?: Product;
  onSave: (data: Omit<Product, "id">) => void;
  onCancel: () => void;
  categories: CategoryOption[];
}

export function ProductForm({ product, onSave, onCancel, categories }: ProductFormProps) {
  const generateQrCodeValue = () => {
    if (typeof crypto === "undefined") {
      return "";
    }

    return crypto.randomUUID();
  };

  const [form, setForm] = useState(() => ({
    name: product?.name || "",
    quantity: product?.quantity || 0,
    description: product?.description || "",
    photoUrl: product?.photoUrl || "",
    qrCode: product?.qrCode || generateQrCodeValue(),
    price: product?.price || 0,
    categoryId: product?.categoryId || "",
  }));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const normalizedCategoryId = form.categoryId ? (typeof form.categoryId === "string" ? Number(form.categoryId) : form.categoryId) : undefined;
    onSave({ ...form, categoryId: normalizedCategoryId });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">Name</label>
        <input
          type="text"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">Quantity</label>
        <input
          type="number"
          value={form.quantity}
          onChange={(e) => setForm({ ...form, quantity: Number(e.target.value) })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">Price</label>
        <input
          type="number"
          step="0.01"
          value={form.price}
          onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">Category</label>
        <select
          value={form.categoryId}
          onChange={(e) => setForm({ ...form, categoryId: e.target.value ? Number(e.target.value) : "" })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
          required
        >
          <option value="">Select a category</option>
          {categories.map((category) => (
            <option key={category.categoryId} value={category.categoryId}>
              {category.categoryName}
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">Description</label>
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">Photo URL</label>
        <input
          type="url"
          value={form.photoUrl}
          onChange={(e) => setForm({ ...form, photoUrl: e.target.value })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">QR Code</label>
        <input
          type="text"
          value={form.qrCode}
          onChange={(e) => setForm({ ...form, qrCode: e.target.value })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
        />
      </div>
      <div className="rounded-xl border border-[#f0dfc5] bg-[#fffaf3] p-4">
        <div className="text-sm font-medium text-[#5b4a37]">Scannable QR preview</div>
        <div className="mt-3 flex justify-center rounded-lg bg-white p-4">
          {form.qrCode ? (
            <QRCodeCanvas value={form.qrCode} size={180} includeMargin />
          ) : (
            <span className="text-sm text-[#8a6b45]">Generate or enter a QR value to preview it.</span>
          )}
        </div>
        <p className="mt-3 break-all text-xs text-[#8a6b45]">{form.qrCode || "No QR value yet."}</p>
      </div>
      <div className="flex gap-2">
        <button
          type="submit"
          className="rounded-md bg-[#f59e0b] px-4 py-2 text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md"
        >
          {product ? "Update" : "Add"} Product
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md bg-[#8a6b45] px-4 py-2 text-white shadow-sm transition hover:bg-[#6a5841] hover:shadow-md"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}