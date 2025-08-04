package com.leafymart.Manager;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leafymart.Structure.ApiConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class CartManager {

    private static CartManager instance;
    private Context context;

    private CartManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public interface CartCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void addToCart(int userId, int productId, int quantity, CartCallback callback) {
        String url = ApiConfig.BASE_URL + "/cart";

        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("product_id", productId);
            json.put("quantity", quantity);
        } catch (JSONException e) {
            callback.onFailure(e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, json,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(response.getString("message"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Error parsing response");
                    }
                },
                error -> callback.onFailure(error.getMessage())
        );

        Volley.newRequestQueue(context).add(request);
    }


    public void updateQuantity(int userId, int productId, int quantity, CartCallback callback) {
        String url = ApiConfig.BASE_URL + "/cart/update";

        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("product_id", productId);
            json.put("quantity", quantity);
        } catch (JSONException e) {
            callback.onFailure(e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, json,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(response.getString("message"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Error parsing response");
                    }
                },
                error -> callback.onFailure(error.getMessage())
        );

        Volley.newRequestQueue(context).add(request);
    }
}
    // You can add methods for updateQuantity, removeFromCart etc. similarly.

