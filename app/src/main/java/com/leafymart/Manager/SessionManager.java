package com.leafymart.Manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.leafymart.Model.App;
import com.leafymart.Structure.ApiConfig;

public class SessionManager {
    private static final String PREF_NAME = "LeafySession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_ADMIN_ID = "admin_id";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }


    public void saveUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public void setUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void setUserName(String userName) {
        prefs.edit().putString(KEY_USER_NAME, userName).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void clear() {
        int userId = getUserId(); // Save before clearing
        clearCartFromServer(userId); // Call this before clearing prefs
        prefs.edit().clear().apply();
    }

    private void clearCartFromServer(int userId) {
            if (userId == -1) return; // No valid user

            String url = ApiConfig.BASE_URL + "/cart/clear?user_id=" + userId;

            StringRequest request = new StringRequest(Request.Method.DELETE, url,
                    response -> {
                        // Optional: Log success
                    },
                    error -> {
                        // Optional: Log failure
                    });

            Volley.newRequestQueue(App.getContext()).add(request);  // use Application context
        }

    public static void saveAdminId(Context context, int adminId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_ADMIN_ID, adminId).apply();
    }

    public static int getAdminId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ADMIN_ID, -1);
    }

    public static void saveUserId(Context context, int userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }


    }
