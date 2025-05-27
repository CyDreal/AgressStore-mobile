package com.example.agress.api.response;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {
    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}