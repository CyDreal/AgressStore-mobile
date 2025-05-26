package com.example.agress.api.response;

import com.example.agress.model.User;
import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("avatar")
    private String avatar;

    // Getters
    public int getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
    public User getUser() {
        return user;
    }
    public String getAvatar() {
        return avatar;
    }
}
