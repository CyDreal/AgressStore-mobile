package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("quantity")
    private int quantity;
    @SerializedName("price")
    private String price;
    @SerializedName("product")
    private Product product;

    // Getters
    public int getQuantity() { return quantity; }
    public String getPrice() { return price; }
    public Product getProduct() { return product; }
}