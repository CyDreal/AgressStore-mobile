package com.example.agress.api;

import com.example.agress.api.response.AddressResponse;
import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.CartListResponse;
import com.example.agress.api.response.CartResponse;
import com.example.agress.api.response.CourierResponse;
import com.example.agress.api.response.CreateOrderResponse;
import com.example.agress.api.response.MidtransResponse;
import com.example.agress.api.response.OrderDetailResponse;
import com.example.agress.api.response.OrderResponse;
import com.example.agress.api.response.ProductDetailResponse;
import com.example.agress.api.response.ProductResponse;
import com.example.agress.api.response.RajaOngkirResponse;
import com.example.agress.api.response.ShippingCostResponse;
import com.example.agress.api.response.UserResponse;
import com.example.agress.api.response.request.LoginRequest;
import com.example.agress.api.response.request.RegisterRequest;
import com.example.agress.api.response.request.UpdateProfileRequest;
import com.example.agress.model.City;
import com.example.agress.model.Province;

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
    @POST("api/user/reset-password")
    Call<BaseResponse> resetPassword(
        @Query("user_id") String userId,
        @Query("old_password") String oldPassword,
        @Query("new_password") String newPassword,
        @Query("confirm_password") String confirmPassword
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

    // rajaongkir
    @GET("api/provinces")
    Call<RajaOngkirResponse<Province>> getProvinces();
    @GET("api/cities")
    Call<RajaOngkirResponse<City>> getCities(@Query("province") String provinceId);

    // shipping address api
    @FormUrlEncoded
    @POST("api/shipping/address")
    Call<BaseResponse> saveAddress(
            @Field("user_id") String userId,
            @Field("label") String label,
            @Field("recipient_name") String recipientName,
            @Field("phone") String phone,
            @Field("province_id") String provinceId,
            @Field("province_name") String provinceName,
            @Field("city_id") String cityId,
            @Field("city_name") String cityName,
            @Field("full_address") String address,
            @Field("postal_code") String postalCode,
            @Field("notes") String notes
    );
    @GET("api/shipping/address/{user_id}")
    Call<AddressResponse> getAddresses(@Path("user_id") String userId);
    @GET("api/shipping/couriers")
    Call<CourierResponse> getCouriers();
    @POST("api/shipping/calculate")
    Call<ShippingCostResponse> calculateShipping(
        @Query("origin") String origin,
        @Query("destination") String destination,
        @Query("weight") int weight,
        @Query("courier") String courier
    );

    // orders api
    @GET("api/orders/{id}")
    Call<OrderDetailResponse> getOrderDetail(@Path("id") int orderId);
    @GET("api/user/{id}/orders")
    Call<OrderResponse> getUserOrders(@Path("id") String userId);
    @POST("api/orders")
    Call<CreateOrderResponse> createOrder(@Body Map<String, Object> orderData);

    // payment api
    @POST("api/payments/create")
    Call<MidtransResponse> createMidtransPayment(@Body Map<String, String> paymentData);
}
