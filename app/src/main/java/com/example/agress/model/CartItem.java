package com.example.agress.model;

public class CartItem {
    private int id;
    private int productId;
    private String productName;
    private int price;
    private int quantity;
    private String imageUrl;
    private int stock;

    public CartItem(int productId, String productName, int price, int quantity, String imageUrl, int stock) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.stock = stock;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getImageUrl() { return imageUrl; }
    public int getStock() { return stock; }
}