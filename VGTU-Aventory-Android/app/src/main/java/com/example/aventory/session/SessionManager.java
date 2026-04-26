package com.example.aventory.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "aventory_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, int userId, String email) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isBlank();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, 0);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }
}
