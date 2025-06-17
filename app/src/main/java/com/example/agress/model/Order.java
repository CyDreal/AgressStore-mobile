package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Order {
    @SerializedName("id")
    private int id;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("payment_method")
    private String paymentMethod;
    @SerializedName("payment_status")
    private String paymentStatus;
    @SerializedName("payment_token")
    private String paymentToken;
    @SerializedName("payment_url")
    private String paymentUrl;
    @SerializedName("shipping_address")
    private String shippingAddress;
    @SerializedName("shipping_city")
    private String shippingCity;
    @SerializedName("shipping_province")
    private String shippingProvince;
    @SerializedName("shipping_postal_code")
    private String shippingPostalCode;
    @SerializedName("shipping_cost")
    private String shippingCost;
    @SerializedName("total_price")
    private String totalPrice;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("items")
    private List<OrderItem> items;

    // Getters
    public int getId() { return id; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaymentToken() { return paymentToken; }
    public String getPaymentUrl() { return paymentUrl; }
    public String getShippingAddress() { return shippingAddress; }
    public String getShippingCity() { return shippingCity; }
    public String getShippingProvince() { return shippingProvince; }
    public String getShippingPostalCode() { return shippingPostalCode; }
    public String getShippingCost() { return shippingCost; }
    public String getTotalPrice() { return totalPrice; }
    public String getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}