package com.example.agress.api.response.request;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("username")
    private String username;
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

    public UpdateProfileRequest(String username, String address, String city,
                              String province, String phone, String postalCode) {
        this.username = username;
        this.address = address;
        this.city = city;
        this.province = province;
        this.phone = phone;
        this.postalCode = postalCode;
    }
}
