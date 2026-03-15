import Link from "next/link";

export default function Home() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-6 py-12 text-slate-900">
      <div className="w-full max-w-xl rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="text-3xl font-bold">Inventory</h1>
        <p className="mt-3 text-slate-600">
          Browse the product catalogue from the products page.
        </p>
        <Link
          href="/products"
          className="mt-6 inline-block rounded-md bg-slate-900 px-5 py-2.5 text-sm font-semibold text-white hover:bg-slate-700"
        >
          Go to Products
        </Link>
      </div>
    </main>
  );
}
