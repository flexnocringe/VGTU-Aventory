package com.example.aventory.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AventoryApi {
    @POST("auth/login")
    Call<ApiModels.LoginResponse> login(@Body ApiModels.LoginRequest request);

    @GET("api/products/scan")
    Call<ApiModels.ProductScanResponse> getProductByQr(
            @Header("Authorization") String authorization,
            @Query("qrCode") String qrCode
    );

    @POST("sales")
    Call<Void> createSale(
            @Header("Authorization") String authorization,
            @Body ApiModels.SaleRequest request
    );
}
