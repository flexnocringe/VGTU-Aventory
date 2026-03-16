import { ApiProduct, Product } from "./types";

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