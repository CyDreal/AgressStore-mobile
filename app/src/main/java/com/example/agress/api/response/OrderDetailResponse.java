package com.example.agress.api.response;

import com.example.agress.model.Order;
import com.google.gson.annotations.SerializedName;

public class OrderDetailResponse {
    @SerializedName("status")
    private int status;
    @SerializedName("order")
    private Order order;

    public int getStatus() { return status; }
    public Order getOrder() { return order; }
}