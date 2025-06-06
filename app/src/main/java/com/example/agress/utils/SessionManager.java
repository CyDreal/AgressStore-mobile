package com.example.agress.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.agress.model.CartItem;
import com.example.agress.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_CITY = "city";
    private static final String KEY_PROVINCE = "province";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_POSTAL_CODE = "postal_code";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_IS_GUEST = "isGuest";
    private static final String KEY_USER = "user";
    private final Gson gson = new Gson();
    private static final String KEY_CART = "cart";
    private static final String KEY_CART_COUNT = "cart_count";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Metod Getter
    public String getUserId() {
        String userId = pref.getString(KEY_USER_ID, "");
        // Debug log
        System.out.println("Debug - Getting UserId from Session: " + userId);
        return userId;
    }
    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }
    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }
    public String getAddress() {
        return pref.getString(KEY_ADDRESS, "");
    }
    public String getCity() {
        return pref.getString(KEY_CITY, "");
    }
    public String getProvince() {
        return pref.getString(KEY_PROVINCE, "");
    }
    public String getPhone() {
        return pref.getString(KEY_PHONE, "");
    }
    public String getPostalCode() {
        return pref.getString(KEY_POSTAL_CODE, "");
    }
    public String getAvatarPath() {
        return pref.getString(KEY_AVATAR, "");
    }

    public void saveAvatarPath(String avatar) {
        if (avatar != null) {
            System.out.println("Debug - Saving Avatar Path: " + avatar);
            editor.putString(KEY_AVATAR, avatar);
            editor.commit(); // Use commit() instead of apply() for immediate effect
        }
    }

    public void updateProfile(String username, String address, String city, String province, String phone, String postalCode) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ADDRESS, address);
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_PROVINCE, province);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_POSTAL_CODE, postalCode);
        editor.commit(); // Use commit() instead of apply() for immediate effect | gunakan apply() untuk menyimpan data secara asinkron
    }

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public User getUser() {
        String userJson = pref.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }

        // If no JSON stored, create User object from individual fields
        if (isLoggedIn()) {
            User user = new User();
            user.setUserId(getUserId());
            user.setUsername(getUsername());
            user.setEmail(getEmail());
            user.setAddress(getAddress());
            user.setCity(getCity());
            user.setProvince(getProvince());
            user.setPhone(getPhone());
            user.setPostalCode(getPostalCode());
            return user;
        }

        return null;
    }

    public void saveUser(User user) {
        if (user != null) {
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER, userJson);
            editor.putString(KEY_USER_ID, user.getUserId());
            editor.putString(KEY_USERNAME, user.getUsername());
            editor.putString(KEY_EMAIL, user.getEmail());
            editor.putString(KEY_ADDRESS, user.getAddress());
            editor.putString(KEY_CITY, user.getCity());
            editor.putString(KEY_PROVINCE, user.getProvince());
            editor.putString(KEY_PHONE, user.getPhone());
            editor.putString(KEY_POSTAL_CODE, user.getPostalCode());
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.commit();
        }
    }

    public void createLoginSession(String userId, String username, String email, String address, String city, String province, String phone, String postalCode) {
        // Validasi input
        if (userId == null || userId.isEmpty()) {
            System.out.println("Debug - UserId is null or empty");
            return;
        }

        // Membersihkan session sebelumnya
        editor.clear();

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ADDRESS, address);
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_PROVINCE, province);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_POSTAL_CODE, postalCode);

        boolean success = editor.commit(); // Use commit() instead of apply() for immediate effect

        // Verify data tersimpan
        System.out.println("Debug - Session Created - Success: " + success);
        System.out.println("Debug - Session Created - UserId: " + pref.getString(KEY_USER_ID, ""));
        System.out.println("Debug - Session Created - IsLoggedIn: " + pref.getBoolean(KEY_IS_LOGGED_IN, false));
    }

    public boolean isLoggedIn() {
        boolean isLoggedIn = pref.getBoolean(KEY_IS_LOGGED_IN,false);
        // Debug log
        System.out.println("Debug - IsLoggedIn: " + isLoggedIn);
        return isLoggedIn;
    }

    public void createGuestSession() {
        editor.clear();
        editor.putBoolean(KEY_IS_GUEST, true);
        editor.apply(); // menggunakan apply() untuk penyimpanan asynchronous
    }

    public boolean isGuest() {
        return pref.getBoolean(KEY_IS_GUEST, false);
    }

    public void logout() {
        editor.clear();
        editor.apply(); // menggunakan apply() untuk penyimpanan asynchronous
    }

    public void addToCart(CartItem item) {
        String cartJson = pref.getString(KEY_CART, "{}");
        Type type = new TypeToken<Map<Integer, CartItem>>(){}.getType();
        Map<Integer, CartItem> cart = gson.fromJson(cartJson, type);

        // Langsung masukkan item baru, tanpa menambahkan quantity
        cart.put(item.getProductId(), item);

        editor.putString(KEY_CART, gson.toJson(cart));
        editor.putInt(KEY_CART_COUNT, cart.size());
        editor.apply();
    }

    public List<CartItem> getCartItems() {
        String cartJson = pref.getString(KEY_CART, "{}");
        Type type = new TypeToken<Map<Integer, CartItem>>(){}.getType();
        Map<Integer, CartItem> cart = gson.fromJson(cartJson, type);
        return new ArrayList<>(cart.values());
    }

    public int getCartCount() {
        return pref.getInt(KEY_CART_COUNT, 0);
    }

//    public void clearCart() {
//        editor.remove(KEY_CART);
//        editor.remove(KEY_CART_COUNT);
//        editor.apply();
//    }

    public void updateCartItemQuantity(int productId, int newQuantity) {
        String cartJson = pref.getString(KEY_CART, "{}");
        Type type = new TypeToken<Map<Integer, CartItem>>(){}.getType();
        Map<Integer, CartItem> cart = gson.fromJson(cartJson, type);

        CartItem item = cart.get(productId);
        if (item != null) {
            // Validasi quantity
            if (newQuantity > 0 && newQuantity <= item.getStock()) {
                item.setQuantity(newQuantity);
                cart.put(productId, item);
                editor.putString(KEY_CART, gson.toJson(cart));
                editor.apply();
            }
        }
    }

    public void removeFromCart(int productId) {
        String cartJson = pref.getString(KEY_CART, "{}");
        Type type = new TypeToken<Map<Integer, CartItem>>(){}.getType();
        Map<Integer, CartItem> cart = gson.fromJson(cartJson, type);

        if (cart.containsKey(productId)) {
            cart.remove(productId);
            editor.putString(KEY_CART, gson.toJson(cart));
            editor.putInt(KEY_CART_COUNT, Math.max(0, getCartCount() - 1));
            editor.apply();
        }
    }
}
