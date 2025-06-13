package com.example.agress.model;

public class Cart {
    private int id;
    private String user_id;
    private int product_id;
    private int quantity;
    private Product product;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public int getProductId() {
        return product_id;
    }

    public void setProductId(int product_id) {
        this.product_id = product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // Convert Cart to CartItem
    public CartItem toCartItem() {
        if (this.product != null && this.product.getImages() != null
                && !this.product.getImages().isEmpty()) {
            CartItem item = new CartItem(
                    this.product.getId(),
                    this.product.getProductName(),
                    this.product.getPrice(),
                    this.quantity,
                    this.product.getImages().get(0).getImageUrl(),
                    this.product.getStock()
            );
            item.setId(this.id); // Set cart ID
            return item;
        }
        return null;
    }
}
