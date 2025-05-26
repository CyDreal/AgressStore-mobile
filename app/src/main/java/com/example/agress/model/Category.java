package com.example.agress.model;

public class Category {
    private int iconId;
    private String name;

    public Category(int iconId, String name) {
        this.iconId = iconId;
        this.name = name;
    }

    public int getIconId() { return iconId; }
    public String getName() { return name; }
}
