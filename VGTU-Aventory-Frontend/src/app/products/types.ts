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