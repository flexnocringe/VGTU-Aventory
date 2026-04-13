type DashboardSummary = {
  totalProducts: number;
  totalStock: number;
  lowStockCount: number;
  categories: number;
};

type DashboardSummaryCardsProps = {
  summary: DashboardSummary;
  loading: boolean;
};

function SummaryCard({
  title,
  value,
  description,
}: {
  title: string;
  value: string | number;
  description: string;
}) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <p className="text-sm font-medium text-slate-500">{title}</p>
      <p className="mt-2 text-3xl font-semibold text-slate-900">{value}</p>
      <p className="mt-1 text-sm text-slate-600">{description}</p>
    </div>
  );
}

export function DashboardSummaryCards({ summary, loading }: DashboardSummaryCardsProps) {
  return (
    <section className="grid gap-6 md:grid-cols-2 xl:grid-cols-4">
      <SummaryCard
        title="Total products"
        value={loading ? "-" : summary.totalProducts}
        description="Unique SKUs currently listed"
      />
      <SummaryCard
        title="Total stock"
        value={loading ? "-" : summary.totalStock}
        description="Total quantity across all products"
      />
      <SummaryCard
        title="Low stock items"
        value={loading ? "-" : summary.lowStockCount}
        description="Products at or below threshold (20)"
      />
      <SummaryCard
        title="Categories"
        value={loading ? "-" : summary.categories}
        description="Category tracking not connected yet"
      />
    </section>
  );
}
