package com.example.agress.api.response;

import java.util.List;

public class ShippingCostResponse {
    private RajaOngkir rajaongkir;

    public RajaOngkir getRajaOngkir() { return rajaongkir; }

    public static class RajaOngkir {
        private List<Result> results;
        public List<Result> getResults() { return results; }
    }

    public static class Result {
        private List<Cost> costs;
        public List<Cost> getCosts() { return costs; }
    }

    public static class Cost {
        private String service;
        private String description;
        private List<CostDetail> cost;

        public String getService() { return service; }
        public String getDescription() { return description; }
        public List<CostDetail> getCost() { return cost; }
    }

    public static class CostDetail {
        private int value;
        private String etd;

        public int getValue() { return value; }
        public String getEtd() { return etd; }
    }
}