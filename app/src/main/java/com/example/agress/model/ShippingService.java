package com.example.agress.model;

public class ShippingService {
    private String service;
    private String description;
    private ShippingCost cost;

    public static class ShippingCost {
        private int value;
        private String etd;

        public int getValue() { return value; }
        public String getEtd() { return etd; }
        public void setValue(int value) { this.value = value; }
        public void setEtd(String etd) { this.etd = etd; }
    }

    public String getService() { return service; }
    public String getDescription() { return description; }
    public ShippingCost getCost() { return cost; }

    public void setService(String service) { this.service = service; }
    public void setDescription(String description) { this.description = description; }
    public void setCost(ShippingCost cost) { this.cost = cost; }
}