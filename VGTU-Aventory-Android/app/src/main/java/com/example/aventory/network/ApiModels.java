package com.example.aventory.network;

public class ApiModels {
    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class LoginResponse {
        public String token;
        public int id;
        public String email;
        public String role;
    }

    public static class ProductScanResponse {
        public int productId;
        public String productName;
        public double price;
        public String productDescription;
        public String photoUrl;
        public int quantity;
        public String qrCode;
    }

    public static class SaleRequest {
        public int productId;
        public int ownerId;
        public int quantity;
        public String saleLocation;
        public String saleType;
        public String saleNote;

        public SaleRequest(int productId, int ownerId, int quantity, String saleLocation, String saleType, String saleNote) {
            this.productId = productId;
            this.ownerId = ownerId;
            this.quantity = quantity;
            this.saleLocation = saleLocation;
            this.saleType = saleType;
            this.saleNote = saleNote;
        }
    }
}
