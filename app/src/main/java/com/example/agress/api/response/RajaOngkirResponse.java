package com.example.agress.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RajaOngkirResponse<T> {
    @SerializedName("rajaongkir")
    private RajaOngkir<T> rajaongkir;

    public static class RajaOngkir<T> {
        @SerializedName("results")
        private List<T> results;

        public List<T> getResults() {
            return results;
        }
    }

    public RajaOngkir<T> getRajaongkir() {
        return rajaongkir;
    }
}
