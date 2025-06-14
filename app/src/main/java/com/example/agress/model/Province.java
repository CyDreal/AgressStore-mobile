package com.example.agress.model;

import com.google.gson.annotations.SerializedName;

public class Province {
    @SerializedName("province_id")
    private String provinceId;
    @SerializedName("province")
    private String provinceName;

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public String toString() {
        return provinceName;
    }
}

