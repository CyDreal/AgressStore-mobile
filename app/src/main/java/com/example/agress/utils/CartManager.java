package com.example.agress.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.agress.model.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static final String PREF_NAME = "GuestCart";
    private static final String KEY_CART_ITEMS = "guest_cart_items";
    private static final String KEY_CART_COUNT = "guest_cart_count";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public CartManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void addToCart(CartItem item) {
        Map<Integer, CartItem> cart = getCartMap();
        cart.put(item.getProductId(), item);
        saveCart(cart);
    }

    public void updateCartItem(CartItem item) {
        Map<Integer, CartItem> cart = getCartMap();
        if (cart.containsKey(item.getProductId())) {
            cart.put(item.getProductId(), item);
            saveCart(cart);
        }
    }

    public void removeFromCart(int productId) {
        Map<Integer, CartItem> cart = getCartMap();
        if (cart.containsKey(productId)) {
            cart.remove(productId);
            saveCart(cart);
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(getCartMap().values());
    }

    public void clearCart() {
        editor.clear().apply();
    }

    public int getCartItemCount() {
        return pref.getInt(KEY_CART_COUNT, 0);
    }

    public Map<Integer, CartItem> getCartMap() {
        String cartJson = pref.getString(KEY_CART_ITEMS, "{}");
        Type type = new TypeToken<Map<Integer, CartItem>>(){}.getType();
        Map<Integer, CartItem> cart = gson.fromJson(cartJson, type);
        return cart != null ? cart : new HashMap<>();
    }

    private void saveCart(Map<Integer, CartItem> cart) {
        String cartJson = gson.toJson(cart);
        editor.putString(KEY_CART_ITEMS, cartJson);
        editor.putInt(KEY_CART_COUNT, cart.size());
        editor.apply();
    }

    public boolean hasItem(int productId) {
        return getCartMap().containsKey(productId);
    }

    public void updateItemQuantity(int productId, int newQuantity) {
        Map<Integer, CartItem> cart = getCartMap();
        if (cart.containsKey(productId)) {
            CartItem item = cart.get(productId);
            if (item != null) {
                item.setQuantity(newQuantity);
                saveCart(cart);
            }
        }
    }

}