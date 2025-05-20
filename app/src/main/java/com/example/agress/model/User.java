package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String userId;
    @SerializedName("username")
    private String username;
    @SerializedName("email")
    private String email;
    @SerializedName("address")
    private String address;
    @SerializedName("city")
    private String city;
    @SerializedName("province")
    private String province;
    @SerializedName("phone")
    private String phone;
    @SerializedName("postal_code")
    private String postalCode;
    @SerializedName("avatar")
    private String avatar;

    // Getters and setters
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
