package com.example.agress.api.response;

import com.example.agress.model.Order;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderResponse {
    @SerializedName("status")
    private int status;
    @SerializedName("orders")
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }
}