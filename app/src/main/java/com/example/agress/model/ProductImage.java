package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

public class ProductImage {
    @SerializedName("id")
    private int id;
    @SerializedName("product_id")
    private int productId;
    @SerializedName("image_path")
    private String imagePath;
    @SerializedName("image_order")
    private int imageOrder;
    @SerializedName("image_url")
    private String imageUrl;

// Getters

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getImageOrder() {
        return imageOrder;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
