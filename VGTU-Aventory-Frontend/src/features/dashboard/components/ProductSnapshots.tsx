import { Product } from "@/features/dashboard/types/product";

type ProductSnapshotsProps = {
  products: Product[];
  loading: boolean;
};

export function ProductSnapshots({ products, loading }: ProductSnapshotsProps) {
  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Product snapshots</h2>
          <p className="mt-1 text-sm text-slate-600">Quick look at recently loaded products.</p>
        </div>
        {loading ? <span className="text-sm font-medium text-slate-500">Loading...</span> : null}
      </div>

      <div className="mt-5 space-y-3">
        {loading ? (
          <p className="text-sm text-slate-500">Fetching products from API...</p>
        ) : products.length === 0 ? (
          <p className="text-sm text-slate-500">No products returned from the API.</p>
        ) : (
          products.slice(0, 5).map((product) => (
            <article
              key={product.id}
              className="flex items-center justify-between rounded-xl border border-slate-100 bg-slate-50 px-4 py-3"
            >
              <div className="min-w-0">
                <p className="truncate font-medium text-slate-900">{product.name}</p>
                <p className="truncate text-xs text-slate-500">{product.description}</p>
              </div>
              <div className="pl-4 text-right">
                <p className="text-sm font-semibold text-slate-900">{product.quantity}</p>
                <p className="text-xs text-slate-500">{product.qrCode}</p>
              </div>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
