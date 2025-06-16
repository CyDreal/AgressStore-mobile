package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("id")
    private int id;
    @SerializedName("order_id")
    private int orderId;
    @SerializedName("product_id")
    private int productId;
    @SerializedName("quantity")
    private int quantity;
    @SerializedName("price")
    private String price;
    @SerializedName("subtotal")
    private String subtotal;
    @SerializedName("product")
    private Product product;

    public String getSubtotal() { return subtotal; }
    public int getQuantity() { return quantity; }
    public String getPrice() { return price; }
    public Product getProduct() { return product; }
}