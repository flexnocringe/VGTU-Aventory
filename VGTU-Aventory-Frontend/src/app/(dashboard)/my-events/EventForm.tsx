import { useState } from "react";

interface EventFormProps {
  onSave: (data: { startDate: string; endDate: string; description: string }) => void;
  onCancel: () => void;
  initialData?: { startDate: string; endDate: string; description: string };
  isEditing?: boolean;
}

export function EventForm({ onSave, onCancel, initialData, isEditing = false }: EventFormProps) {
  const formatDateForInput = (dateString?: string) => {
    if (!dateString) return "";
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return "";
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const [form, setForm] = useState({
    startDate: formatDateForInput(initialData?.startDate),
    endDate: formatDateForInput(initialData?.endDate),
    description: initialData?.description || "",
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(form);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
      <h2 className="text-xl font-bold text-[#2d2418]">
        {isEditing ? "Edit Event" : "Create New Event"}
      </h2>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">Start Date & Time</label>
        <input
          type="datetime-local"
          value={form.startDate}
          onChange={(e) => setForm({ ...form, startDate: e.target.value })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20 text-[#2d2418]"
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">End Date & Time</label>
        <input
          type="datetime-local"
          value={form.endDate}
          onChange={(e) => setForm({ ...form, endDate: e.target.value })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20 text-[#2d2418]"
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-[#5b4a37]">Description</label>
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          className="mt-1 block w-full rounded-md border border-[#e9dfcc] bg-white px-3 py-2 shadow-sm outline-none focus:border-[#f59e0b] focus:ring-2 focus:ring-[#f59e0b]/20 text-[#2d2418]"
          rows={3}
        />
      </div>
      <div className="flex gap-2 pt-2">
        <button
          type="submit"
          className="rounded-md bg-[#f59e0b] px-4 py-2 text-white shadow-sm transition hover:bg-[#ea8c08] hover:shadow-md font-semibold"
        >
          {isEditing ? "Update Event" : "Create Event"}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md bg-[#8a6b45] px-4 py-2 text-white shadow-sm transition hover:bg-[#6a5841] hover:shadow-md font-semibold"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
