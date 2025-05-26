package com.example.agress.api;

import com.example.agress.api.response.ProductDetailResponse;
import com.example.agress.api.response.ProductResponse;
import com.example.agress.api.response.UserResponse;
import com.example.agress.api.response.request.LoginRequest;
import com.example.agress.api.response.request.RegisterRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/login")
    Call<UserResponse> login(@Body LoginRequest loginRequest);
    @POST("api/register")
    Call<UserResponse> register(@Body RegisterRequest registerRequest);

    // product api
    @GET("api/products")
    Call<ProductResponse> getProducts();
    @GET("api/products/{id}")
    Call<ProductDetailResponse> getProductDetail(@Path("id") int productId);
    @POST("api/products/view-count")
    Call<ProductResponse> updateViewCount(@Body Map<String, Integer> productId);
}
