package com.example.agress.api.response.request;

import com.google.gson.annotations.SerializedName;

public class PasswordResetRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("old_password")
    private String oldPassword;
    @SerializedName("new_password")
    private String newPassword;
    @SerializedName("confirm_password")
    private String confirmPassword;

    public PasswordResetRequest(String userId, String oldPassword, String newPassword, String confirmPassword) {
        this.userId = userId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
