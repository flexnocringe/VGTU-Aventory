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
  price: number;
};

export function mapApiProduct(p: ApiProduct): Product {
  return {
    id: String(p.productId),
    name: p.productName,
    quantity: p.quantity,
    description: p.productDescription,
    photoUrl: p.photoUrl,
    qrCode: p.qrCode,
    price: p.price,
  };
}