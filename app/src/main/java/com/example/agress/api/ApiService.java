package com.example.agress.api;

import com.example.agress.api.response.UserResponse;
import com.example.agress.api.response.request.LoginRequest;
import com.example.agress.api.response.request.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/login")
    Call<UserResponse> login(@Body LoginRequest loginRequest);

    @POST("api/register")
    Call<UserResponse> register(@Body RegisterRequest registerRequest);
}
