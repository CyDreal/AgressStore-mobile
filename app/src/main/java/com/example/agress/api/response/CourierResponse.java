package com.example.agress.api.response;

import com.example.agress.model.Courier;

import java.util.List;

public class CourierResponse {
    private int status;
    private String origin;
    private List<Courier> couriers;

    // Getters and setters
    public int getStatus() { return status; }
    public String getOrigin() { return origin; }
    public List<Courier> getCouriers() { return couriers; }
}