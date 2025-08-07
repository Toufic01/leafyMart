package com.leafymart.Structure;

import android.content.Context;
// Import the Android framework Log class
import android.util.Log;

import androidx.annotation.OptIn; // Keep if you use other UnstableApi features
import androidx.media3.common.util.UnstableApi; // Keep if you use other UnstableApi features

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
// Assuming MySingleton and ApiConfig are in the same package or correctly imported
// import com.leafymart.Structure.MySingleton;
// import com.leafymart.Structure.ApiConfig;


import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CartHelper {

    // Using android.util.Log for the TAG constant, which is a common practice.
    // If you prefer androidx.media3.common.util.Log for TAG, that's fine too,
    // but the getStackTraceString will still come from android.util.Log.
    private static final String TAG = "CartHelper";

    public interface CartOperationCallback {
        void onSuccess(JSONObject response);
        // Changed signature to match the one used in the previous "solved" code
        void onError(String error, String detailedError);
    }


    // Add this new method for deleting cart items
    @OptIn(markerClass = UnstableApi.class) // If you use media3 Log elsewhere
    public static void deleteCartItem(Context context, int cartItemId, int userId,
                                      CartOperationCallback callback) {

        String url = ApiConfig.BASE_URL + "/cart/" + cartItemId + "?user_id=" + userId;
        // Using androidx.media3.common.util.Log here as per your original file context [1]
        // If you want to switch all logging to android.util.Log, change this too.
        androidx.media3.common.util.Log.d(TAG, "Attempting to delete cart item. URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    androidx.media3.common.util.Log.d(TAG, "Delete item success: " + response.toString());
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            callback.onSuccess(response);
                        } else {
                            String message = response.has("message") ? response.getString("message") : "Delete failed, unknown reason from server.";
                            callback.onError("Server Error: " + message, response.toString());
                        }
                    } catch (JSONException e) {
                        androidx.media3.common.util.Log.e(TAG, "Error parsing success response: ", e);
                        callback.onError("Invalid success response format", e.getMessage());
                    }
                },
                error -> {
                    // Call the corrected handleVolleyError
                    handleVolleyError(TAG, "deleteCartItem", error, callback);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // headers.put("Content-Type", "application/json"); // Not typically needed for DELETE with no body
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.get(context).add(request);
    }


    // Corrected handleVolleyError
    @OptIn(markerClass = UnstableApi.class) // Keep if you use media3 Log elsewhere in this method
    public static void handleVolleyError(String logTag, String operationName, VolleyError error, CartOperationCallback callback) {
        String errorMessage = "Unknown " + operationName + " error";
        String detailedErrorMessage = "No details";
        int statusCode = 0;

        if (error.networkResponse != null) {
            statusCode = error.networkResponse.statusCode;
            if (error.networkResponse.data != null) {
                try {
                    detailedErrorMessage = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    JSONObject errorJson = new JSONObject(detailedErrorMessage);
                    if (errorJson.has("message")) {
                        errorMessage = "Server: " + errorJson.getString("message");
                    } else if (errorJson.has("error")) {
                        errorMessage = "Server: " + errorJson.getString("error");
                    } else {
                        errorMessage = "Server error (Status: " + statusCode + ")";
                    }
                } catch (JSONException e) {
                    errorMessage = "Server error (Status: " + statusCode + ", unparseable body)";
                    // Using androidx.media3.common.util.Log as per your original file context [1]
                    androidx.media3.common.util.Log.w(logTag, operationName + " - Non-JSON error body: " + detailedErrorMessage, e);
                }
            } else {
                errorMessage = "Server error (Status: " + statusCode + ") with no response data.";
            }
        } else if (error.getCause() != null) {
            errorMessage = operationName + " failed: " + error.getCause().getMessage();
            // *** THIS IS THE FIX: Use android.util.Log.getStackTraceString ***
            detailedErrorMessage = android.util.Log.getStackTraceString(error.getCause());
        } else if (error.getMessage() != null) {
            errorMessage = operationName + " failed: " + error.getMessage();
            detailedErrorMessage = error.toString();
        }

        // Using androidx.media3.common.util.Log as per your original file context [1]
        androidx.media3.common.util.Log.e(logTag, operationName + " Error. Status: " + statusCode + ". Message: " + errorMessage + ". Details: " + detailedErrorMessage, error);
        callback.onError(errorMessage, detailedErrorMessage);
    }
}
