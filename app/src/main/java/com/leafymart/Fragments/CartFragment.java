package com.leafymart.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leafymart.Adapter.CartAdapter;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Manager.SessionManager;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ArrayList<PlantModel> cartItems = new ArrayList<>();
    private int userId;
    private Button buyAllButton;

    public CartFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        buyAllButton = view.findViewById(R.id.buyAllButton);

        userId = SessionManager.getInstance(requireContext()).getUserId();

        if (userId != -1) {
            fetchCartDataFromBackend();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        buyAllButton.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            BuyFragment buyFragment = BuyFragment.newInstance(cartItems);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, buyFragment)
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }

    private void fetchCartDataFromBackend() {
        String url = ApiConfig.BASE_URL + "/cart?user_id=" + userId;
        Log.d("CartDebug", "Request URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("CartDebug", "Full response: " + response.toString());

                        if (!response.has("cart")) {
                            throw new JSONException("Missing 'cart' field in response");
                        }

                        JSONArray items = response.getJSONArray("cart");


                        for (int i = 0; i < items.length(); i++) {
                            JSONObject obj = items.getJSONObject(i);
                            Log.d("CartDebug", "Item " + i + ": " + obj.toString());

                            // Verify all required fields exist
                            if (!obj.has("product_id")) {
                                throw new JSONException("Missing product_id in item " + i);
                            }
                            if (!obj.has("name")) {
                                throw new JSONException("Missing name in item " + i);
                            }
                            // Add checks for other required fields

                            PlantModel item = new PlantModel(
                                    obj.getInt("product_id"),
                                    obj.getString("name"),
                                    obj.getDouble("price"),
                                    obj.optString("category", ""),
                                    obj.getString("image_url"),
                                    obj.optString("description", ""),
                                    obj.optDouble("rating", 0.0),
                                    obj.optInt("sold", 0)
                            );

                            if (obj.has("quantity")) {
                                item.setQuantity(obj.getInt("quantity"));
                            }
                            if (obj.has("cart_item_id")) {
                                item.setCartItemId(obj.getInt("cart_item_id"));
                            }

                            cartItems.add(item);
                        }

                        updateCartAdapter();

                    } catch (JSONException e) {
                        Log.e("CartError", "JSON parsing failed", e);
                        showError("Error parsing cart data: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("CartError", "Network error", error);
                    showError("Failed to load cart: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void updateCartAdapter() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            if (cartAdapter == null) {
                cartAdapter = new CartAdapter(requireContext(), cartItems, userId);
                recyclerView.setAdapter(cartAdapter);
            } else {
                cartAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showError(String message) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        });
    }
}
