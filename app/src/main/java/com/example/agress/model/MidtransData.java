package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

public class MidtransData {
    @SerializedName("token")
    private String token;
    @SerializedName("payment_url")
    private String paymentUrl;
    @SerializedName("order_id")
    private int orderId;

    public String getToken() { return token; }
    public String getPaymentUrl() { return paymentUrl; }
    public int getOrderId() { return orderId; }
}