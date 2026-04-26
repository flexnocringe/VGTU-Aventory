export type TotalProfitRequest = {
  startDate: string;
  endDate: string;
};

export type TotalProfitResponse = {
  totalProfit: number;
};

export type TopSellingProductsRequest = {
  startDate: string;
  endDate: string;
};

export type TopSellingProduct = {
  productId: number;
  productName: string;
  salesCount: number;
};
