import { AnalyticsPicker } from "@/features/analytics/components/AnalyticsPicker";
import { AnalyticsProfitCalculator } from "@/features/analytics/components/AnalyticsProfitCalculator";
import { TopSellingProducts } from "@/features/analytics/components/TopSellingProducts";
import { ProductSalesComparison } from "@/features/analytics/components/ProductSalesComparison";
import { AnalyticsDatesProvider } from "@/features/analytics/context/AnalyticsDatesContext";

export default function AnalyticsPage() {
  return (
    <AnalyticsDatesProvider>
      <section className="space-y-6">
        <AnalyticsPicker />
        <AnalyticsProfitCalculator />
        <TopSellingProducts />
        <ProductSalesComparison />
      </section>
    </AnalyticsDatesProvider>
  );
}