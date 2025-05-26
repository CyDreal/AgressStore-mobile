package com.example.agress.api.response;

import com.example.agress.model.Product;
import com.google.gson.annotations.SerializedName;

public class ProductDetailResponse {
    @SerializedName("status")
    private int status;
    @SerializedName("product")
    private Product product;

    // Getters

    public int getStatus() {
        return status;
    }

    public Product getProduct() {
        return product;
    }
}
