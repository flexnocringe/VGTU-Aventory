import { useState } from "react";

type CategoryFormData = {
    categoryName: string;
};

interface CategoryFormProps {
    onSave: (data: CategoryFormData) => void;
    onCancel: () => void;
    isSubmitting?: boolean;
}

export function CategoryForm({ onSave, onCancel, isSubmitting = false }: CategoryFormProps) {
    const [categoryName, setCategoryName] = useState("");

    const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        onSave({ categoryName });
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
                <label htmlFor="category-name" className="block text-sm font-medium text-[#5b4a37]">
                    Category Name
                </label>
                <input
                    id="category-name"
                    type="text"
                    value={categoryName}
                    onChange={(event) => setCategoryName(event.target.value)}
                    className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20"
                    required
                    disabled={isSubmitting}
                />
            </div>

            <div className="flex gap-2">
                <button
                    type="submit"
                    className="rounded-md bg-[#f59e0b] px-4 py-2 text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md disabled:cursor-not-allowed disabled:opacity-60"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? "Creating..." : "Add Category"}
                </button>
                <button
                    type="button"
                    onClick={onCancel}
                    className="rounded-md bg-[#8a6b45] px-4 py-2 text-white shadow-sm transition hover:bg-[#6a5841] hover:shadow-md disabled:cursor-not-allowed disabled:opacity-60"
                    disabled={isSubmitting}
                >
                    Cancel
                </button>
            </div>
        </form>
    );
}