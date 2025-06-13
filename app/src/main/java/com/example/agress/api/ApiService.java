package com.example.agress.api;

import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.CartListResponse;
import com.example.agress.api.response.CartResponse;
import com.example.agress.api.response.ProductDetailResponse;
import com.example.agress.api.response.ProductResponse;
import com.example.agress.api.response.UserResponse;
import com.example.agress.api.response.request.LoginRequest;
import com.example.agress.api.response.request.RegisterRequest;
import com.example.agress.api.response.request.UpdateProfileRequest;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/login")
    Call<UserResponse> login(@Body LoginRequest loginRequest);
    @POST("api/register")
    Call<UserResponse> register(@Body RegisterRequest registerRequest);

    // user api
    @GET("api/user/{id}")
    Call<UserResponse> getUser(@Path("id") String userId);
    @Multipart
    @POST("api/users/{id}/avatar")
    Call<UserResponse> updateAvatar(
        @Path("id") String userId,
        @Part MultipartBody.Part avatar
    );

    // product api
    @GET("api/products")
    Call<ProductResponse> getProducts();
    @GET("api/products/{id}")
    Call<ProductDetailResponse> getProductDetail(@Path("id") int productId);
    @FormUrlEncoded
    @POST("api/products/view-count")
    Call<BaseResponse> updateViewCount(@Field("product_id") int productId);
    @PUT("api/user/{id}")
    Call<UserResponse> updateProfile(@Path("id") String userId, @Body UpdateProfileRequest request);

    // cart api
    @FormUrlEncoded
    @POST("api/carts")
    Call<CartResponse> addToCart(
            @Field("user_id") String userId,
            @Field("product_id") int productId,
            @Field("quantity") int quantity
    );
    @GET("api/carts")
    Call<CartListResponse> getUserCarts(@Query("user_id") String userId);
    @DELETE("api/carts/{cart_id}")
    Call<BaseResponse> removeFromCart(@Path("cart_id") String cartId);
}
