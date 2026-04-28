import { Product } from "@/features/dashboard/types/product";

type ProductSnapshotsProps = {
  products: Product[];
  loading: boolean;
};

export function ProductSnapshots({ products, loading }: ProductSnapshotsProps) {
  return (
    <section className="flex h-full flex-col rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-xl font-semibold text-[#2d2418]">Product snapshots</h2>
          <p className="mt-1 text-sm text-[#6a5841]">Quick look at recently loaded products.</p>
        </div>
        {loading ? <span className="text-sm font-medium text-[#8a6b45]">Loading...</span> : null}
      </div>

      <div className="mt-5 space-y-3">
        {loading ? (
          <p className="text-sm text-[#8a6b45]">Fetching products from API...</p>
        ) : products.length === 0 ? (
          <p className="text-sm text-[#8a6b45]">No products returned from the API.</p>
        ) : (
          products.slice(0, 5).map((product) => (
            <article
              key={product.id}
              className="flex items-center justify-between rounded-xl border border-[#f2e5d1] bg-[#fffaf3] px-4 py-3"
            >
              <div className="min-w-0">
                <p className="truncate font-medium text-[#2d2418]">{product.name}</p>
                <p className="truncate text-xs text-[#8a6b45]">{product.description}</p>
              </div>
              <div className="pl-4 text-right">
                <p className="text-sm font-semibold text-[#2d2418]">{product.quantity}</p>
              </div>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
