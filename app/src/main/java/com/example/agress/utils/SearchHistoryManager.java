package com.example.agress.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryManager {
    private static final String PREF_NAME = "search_history";
    private static final String KEY_HISTORY = "search_queries";
    private static final int MAX_HISTORY = 10;
    private final SharedPreferences preferences;
    private final Gson gson;

    public SearchHistoryManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addQuery(String query) {
        List<String> history = getSearchHistory();
        // Remove if exists to avoid duplicates
        history.remove(query);
        // Add to beginning of list
        history.add(0, query);
        // Trim list if exceeds max size
        if (history.size() > MAX_HISTORY) {
            history = history.subList(0, MAX_HISTORY);
        }
        // Save updated history
        preferences.edit()
                .putString(KEY_HISTORY, gson.toJson(history))
                .apply();
    }

    public List<String> getSearchHistory() {
        String json = preferences.getString(KEY_HISTORY, "[]");
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        return gson.fromJson(json, type);
    }
}
