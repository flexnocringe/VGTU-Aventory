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

import { InventoryMetricCard } from "./InventoryMetricCard";

export function DashboardSummaryCards({ summary, loading }: DashboardSummaryCardsProps) {
  const displayValue = (value: number) => (loading ? "-" : value.toLocaleString());

  return (
    <section className="grid gap-6 md:grid-cols-2 xl:grid-cols-4">
      <InventoryMetricCard
        title="Total Stock"
        value={displayValue(summary.totalStock)}
        accentClassName="bg-[#b56d12]"
        labelClassName="text-[#fff4e2]"
        bgClassName="bg-[#f59e0b]"
        trend="up"
      />
      <InventoryMetricCard
        title="Total Products"
        value={displayValue(summary.totalProducts)}
        accentClassName="bg-[#5a2d13]"
        labelClassName="text-[#fff4e2]"
        bgClassName="bg-[#d97706]"
        trend="up"
      />
      <InventoryMetricCard
        title="Low Stock Items"
        value={displayValue(summary.lowStockCount)}
        accentClassName="bg-[#6b2f16]"
        labelClassName="text-[#fff4e2]"
        bgClassName="bg-[#b45309]"
        trend="down"
      />
      <InventoryMetricCard
        title="Categories"
        value={displayValue(summary.categories)}
        accentClassName="bg-[#8a5a20]"
        labelClassName="text-[#fff4e2]"
        bgClassName="bg-[#92400e]"
        trend="up"
      />
    </section>
  );
}
