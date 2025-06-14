package com.example.agress.api.response;

import com.example.agress.model.Address;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressResponse {
    @SerializedName("status")
    private int status;
    @SerializedName("addresses")
    private List<Address> addresses;

    public List<Address> getAddresses() {
        return addresses;
    }
}