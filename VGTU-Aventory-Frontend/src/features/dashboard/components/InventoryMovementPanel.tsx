import { Product } from "@/features/dashboard/types/product";

type InventoryMovementPanelProps = {
  products: Product[];
  loading: boolean;
};

export function InventoryMovementPanel({ products, loading }: InventoryMovementPanelProps) {
  const barItems = products.slice(0, 7);
  const maxQuantity = Math.max(...barItems.map((item) => item.quantity), 1);

  return (
    <section className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#2d2418]">Stock distribution</h2>
          <p className="mt-1 text-sm text-[#6a5841]">Top products by quantity currently in inventory.</p>
        </div>
        <span className="rounded-full bg-[#fff4e2] px-3 py-1 text-xs font-medium text-[#9a6b2f]">Live</span>
      </div>

      <div className="mt-6">
        {loading ? (
          <p className="text-sm text-[#8a6b45]">Calculating chart values...</p>
        ) : barItems.length === 0 ? (
          <p className="text-sm text-[#8a6b45]">No products available for charting.</p>
        ) : (
          <div className="grid gap-3">
            {barItems.map((product) => {
              const width = Math.round((product.quantity / maxQuantity) * 100);
              return (
                <div key={product.id} className="space-y-1">
                  <div className="flex items-center justify-between text-xs text-[#6a5841]">
                    <span className="truncate pr-3">{product.name}</span>
                    <span className="font-medium text-[#2d2418]">{product.quantity}</span>
                  </div>
                  <div className="h-2 rounded-full bg-[#fff4e2]">
                    <div className="h-full rounded-full bg-gradient-to-r from-[#f59e0b] to-[#9a6b2f]" style={{ width: `${width}%` }} />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </section>
  );
}
