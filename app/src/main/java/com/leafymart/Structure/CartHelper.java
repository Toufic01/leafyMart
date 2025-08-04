package com.leafymart.Structure;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CartHelper {

    public interface CartOperationCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public static void updateCartItem(Context context, int cartItemId, int userId,
                                      int quantity, String status,
                                      CartOperationCallback callback) {
        String url = ApiConfig.BASE_URL + "/cart/" + cartItemId;

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);
            if (quantity > 0) jsonBody.put("quantity", quantity);
            if (status != null) jsonBody.put("status", status);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                callback.onSuccess(response);
                            } else {
                                callback.onError(response.getString("message"));
                            }
                        } catch (Exception e) {
                            callback.onError("Invalid response format");
                        }
                    },
                    error -> {
                        callback.onError(getErrorMessage(error));
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            Volley.newRequestQueue(context).add(request);

        } catch (Exception e) {
            callback.onError("Invalid request data");
        }
    }

    public static void checkoutCart(Context context, int userId, CartOperationCallback callback) {
        String url = ApiConfig.BASE_URL + "/cart/checkout";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                callback.onSuccess(response);
                            } else {
                                callback.onError(response.getString("message"));
                            }
                        } catch (Exception e) {
                            callback.onError("Invalid response format");
                        }
                    },
                    error -> {
                        callback.onError(getErrorMessage(error));
                    }
            );

            Volley.newRequestQueue(context).add(request);

        } catch (Exception e) {
            callback.onError("Invalid request data");
        }
    }

    private static String getErrorMessage(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                return new String(error.networkResponse.data);
            } catch (Exception e) {
                return "Error code: " + error.networkResponse.statusCode;
            }
        }
        return error.getMessage() != null ? error.getMessage() : "Unknown error";
    }
}