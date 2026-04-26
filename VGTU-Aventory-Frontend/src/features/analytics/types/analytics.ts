export type TotalProfitRequest = {
  startDate: string;
  endDate: string;
};

export type TotalProfitResponse = {
  totalProfit: number;
};

export type TotalSalesCountResponse = {
  totalSalesCount: number;
};

export type AnalyticsDateBoundsResponse = {
  firstSaleDate: string;
  today: string;
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

export type ProductSalesComparison = {
  productId: number;
  productName: string;
  totalSalesCount: number;
};

export type AnalyticsDateRange = {
  startDate: string;
  endDate: string;
};
