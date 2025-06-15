package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Order {
    @SerializedName("id")
    private int id;
    @SerializedName("payment_status")
    private String paymentStatus;
    @SerializedName("total_price")
    private String totalPrice;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("items")
    private List<OrderItem> items;

    // Getters
    public int getId() { return id; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getTotalPrice() { return totalPrice; }
    public String getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}