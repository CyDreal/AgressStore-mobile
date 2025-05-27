package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Product {
    @SerializedName("id")
    private int id;
    @SerializedName("product_name")
    private String productName;
    @SerializedName("description")
    private String description;
    @SerializedName("category")
    private String category;
    @SerializedName("price")
    private int price;
    @SerializedName("stock")
    private int stock;
    @SerializedName("weight")
    private int weight;
    @SerializedName("status")
    private String status;
    @SerializedName("purchased_quantity") // Change from purchase_quantity to purchased_quantity
    private int purchaseQuantity;
    @SerializedName("view_count")
    private int viewCount;
    @SerializedName("images")
    private List<ProductImage> images;
    @SerializedName("created_at")
    private String createdAt;

    // Getters

    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public int getWeight() {
        return weight;
    }

    public String getStatus() {
        return status;
    }

    public int getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public int getViewCount() {
        return viewCount;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
