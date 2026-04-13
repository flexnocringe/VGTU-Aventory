export type ApiProduct = {
  productId: number;
  productName: string;
  owner: string | null;
  sales: unknown[];
  price: number;
  productDescription: string;
  photoUrl: string;
  quantity: number;
  qrCode: string;
};

export type Product = {
  id: string;
  name: string;
  quantity: number;
  description: string;
  photoUrl: string;
  qrCode: string;
};

export function mapApiProduct(product: ApiProduct): Product {
  return {
    id: String(product.productId),
    name: product.productName,
    quantity: product.quantity,
    description: product.productDescription,
    photoUrl: product.photoUrl,
    qrCode: product.qrCode,
  };
}
