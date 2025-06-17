package com.example.agress.api.response;

import com.example.agress.model.MidtransData;
import com.google.gson.annotations.SerializedName;

public class MidtransResponse {
    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private MidtransData data;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public MidtransData getData() { return data; }
}