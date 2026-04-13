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
        valueClassName="text-white"
        bgClassName="bg-[#f59e0b]"
        ringClassName="border-[#f2b74e] bg-[#c87d10]"
        trend="up"
      />
      <InventoryMetricCard
        title="Total Products"
        value={displayValue(summary.totalProducts)}
        accentClassName="bg-[#5a2d13]"
        labelClassName="text-[#fff4e2]"
        valueClassName="text-white"
        bgClassName="bg-[#d97706]"
        ringClassName="border-[#f6c17a] bg-[#a8551b]"
        trend="up"
      />
      <InventoryMetricCard
        title="Low Stock Items"
        value={displayValue(summary.lowStockCount)}
        accentClassName="bg-[#6b2f16]"
        labelClassName="text-[#fff4e2]"
        valueClassName="text-white"
        bgClassName="bg-[#b45309]"
        ringClassName="border-[#f6c27f] bg-[#944317]"
        trend="down"
      />
      <InventoryMetricCard
        title="Categories"
        value={displayValue(summary.categories)}
        accentClassName="bg-[#8a5a20]"
        labelClassName="text-[#fff4e2]"
        valueClassName="text-white"
        bgClassName="bg-[#92400e]"
        ringClassName="border-[#f3bf70] bg-[#a46b2b]"
        trend="up"
      />
    </section>
  );
}
