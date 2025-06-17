package com.example.agress.api.response;

import com.google.gson.annotations.SerializedName;

public class CreateOrderResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("order")
    private OrderData order;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public OrderData getOrder() { return order; }

    public static class OrderData {
        @SerializedName("id")
        private int id;

        @SerializedName("user_id")
        private String userId;

        // ... properti lain jika diperlukan

        public int getId() { return id; }
    }
}