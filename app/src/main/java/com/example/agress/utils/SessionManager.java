package com.example.agress.utils;

import android.content.Context;
import android.content.SharedPreferences;

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
}
