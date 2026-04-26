import { AnalyticsProfitCalculator } from "@/features/analytics/components/AnalyticsProfitCalculator";
import { TopSellingProducts } from "@/features/analytics/components/TopSellingProducts";

export default function AnalyticsPage() {
        return (
            <section className="space-y-6">
                <AnalyticsProfitCalculator />
                <TopSellingProducts />
            </section>
        );
}