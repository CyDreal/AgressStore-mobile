package com.example.agress.api.response;

import com.example.agress.model.Cart;

public class CartResponse extends BaseResponse {
    private Cart cart;

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}
