package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

public class City {
    @SerializedName("city_id")
    private String cityId;
    @SerializedName("province_id")
    private String provinceId;
    @SerializedName("province")
    private String province;
    @SerializedName("type")
    private String type;
    @SerializedName("city_name")
    private String cityName;
    @SerializedName("postal_code")
    private String postalCode;

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    @Override
    public String toString() {
        return type + " " + cityName;
    }
}
