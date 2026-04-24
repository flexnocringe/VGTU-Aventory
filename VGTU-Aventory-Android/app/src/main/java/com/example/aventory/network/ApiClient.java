package com.example.aventory.network;

import com.example.aventory.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final Retrofit RETROFIT = new Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final AventoryApi API = RETROFIT.create(AventoryApi.class);

    private ApiClient() {
    }

    public static AventoryApi getApi() {
        return API;
    }
}
