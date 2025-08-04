package com.leafymart.Manager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.leafymart.Fragments.CustomJsonObjectRequest;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Structure.MySingleton;
import com.leafymart.Model.PlantModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteManager {
    private static FavoriteManager INSTANCE;
    private final Context ctx;
    private int userId;
    private final Map<Integer, PlantModel> favoriteProducts = new HashMap<>();

    public interface OnChangeListener {
        void onFavoritesChanged();
    }

    private OnChangeListener listener;

    public static FavoriteManager get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new FavoriteManager(context);
        }
        return INSTANCE;
    }

    private FavoriteManager(Context context) {
        this.ctx = context.getApplicationContext();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
    }

    public boolean isFav(int productId) {
        return favoriteProducts.containsKey(productId);
    }

    public List<PlantModel> getFavoriteProducts() {
        return new ArrayList<>(favoriteProducts.values());
    }

    public void pullFromServer() {
        if (userId <= 0) return;
        String url = ApiConfig.BASE_URL + "/favorites?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray favoritesArray = response.getJSONArray("favorites");
                            handleFavoritesResponse(favoritesArray);
                        } else {
                            String message = response.optString("message", "Failed to load favorites");
                            showToast(message);
                        }
                    } catch (JSONException e) {
                        Log.e("FavoriteManager", "Error parsing favorites", e);
                        showToast("Error loading favorites");
                    }
                },
                error -> {
                    Log.e("FavoriteManager", "Error fetching favorites", error);
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("FavoriteManager", "Error response: " + errorData);
                    }
                    showToast("Network error loading favorites");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.get(ctx).add(request);
    }

    private void handleFavoritesResponse(JSONArray response) {
        try {
            favoriteProducts.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject item = response.getJSONObject(i);
                PlantModel product = parseProduct(item);
                if (product != null) {
                    favoriteProducts.put(product.getId(), product);
                }
            }
            notifyChange();
        } catch (JSONException e) {
            Log.e("FavoriteManager", "Error parsing favorites", e);
            showToast("Error processing favorites");
        }
    }

    private PlantModel parseProduct(JSONObject item) throws JSONException {
        return new PlantModel(
                item.getInt("id"),
                item.getString("name"),
                item.getDouble("price"),
                item.getString("category"),
                item.getString("image_url"),
                item.getString("description"),
                item.getDouble("rating"),
                item.getInt("sold")
        );
    }

    public void add(PlantModel product) {
        String url = ApiConfig.BASE_URL + "/favorites";

        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("product_id", product.getId());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                favoriteProducts.put(product.getId(), product);
                                notifyChange();
                                showToast("Added to favorites");
                            } else {
                                String message = response.optString("message", "Failed to add favorite");
                                showToast(message);
                            }
                        } catch (JSONException e) {
                            Log.e("FavoriteManager", "Error parsing response", e);
                            showToast("Error adding favorite");
                        }
                    },
                    error -> {
                        Log.e("FavoriteManager", "Error adding favorite", error);
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Log.e("FavoriteManager", "Error response: " + errorData);
                        }
                        showToast("Network error adding favorite");
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000, // 10 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MySingleton.get(ctx).add(request);
        } catch (JSONException e) {
            Log.e("FavoriteManager", "Error creating request", e);
            showToast("Error creating favorite request");
        }
    }

    public void removeFavorite(int userId, int productId) {
        // Append user_id and product_id as query parameters
        String url = ApiConfig.BASE_URL + "/favorites?user_id=" + userId + "&product_id=" + productId;

        Log.d("FavoriteManager", "Sending DELETE request to URL: " + url);

        // Use StringRequest, but no need to override getBody() or getBodyContentType()
        // as we are sending data via URL parameters.
        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d("FavoriteManager", "DELETE Response: " + jsonResponse.toString());
                        if (jsonResponse.getBoolean("success")) {
                            favoriteProducts.remove(productId);
                            notifyChange();
                            showToast("Removed from favorites");
                        } else {
                            String message = jsonResponse.optString("message", "Failed to remove favorite");
                            showToast(message);
                        }
                    } catch (JSONException e) {
                        Log.e("FavoriteManager", "Error parsing response", e);
                        showToast("Error removing favorite");
                    }
                },
                error -> {
                    String errorMsg = "Network error";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Log.e("FavoriteManager", "Error response: " + errorData);
                            // Try to parse the errorData as JSON to get a more specific message
                            JSONObject errorJson = new JSONObject(errorData);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (Exception e) {
                            Log.e("FavoriteManager", "Error parsing error response", e);
                        }
                    }
                    Log.e("FavoriteManager", "Error removing favorite", error);
                    showToast(errorMsg);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Accept header is still good to have.
                // Content-Type is not strictly necessary as there is no body.
                headers.put("Accept", "application/json");
                return headers;
            }

            // No need for getBody() or getBodyContentType() anymore
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.get(ctx).add(request);
    }

    // Overloaded remove methods
    public void remove(int productId) {
        removeFavorite(userId, productId);
    }

    public void remove(PlantModel plant) {
        remove(plant.getId());
    }

    private void notifyChange() {
        if (listener != null) {
            listener.onFavoritesChanged();
        }
    }

    private void showToast(String message) {
        if (ctx instanceof Activity) {
            ((Activity) ctx).runOnUiThread(() ->
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
        }
    }
}