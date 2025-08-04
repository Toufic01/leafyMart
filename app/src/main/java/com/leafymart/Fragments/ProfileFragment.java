package com.leafymart.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.leafymart.Adapter.OrderAdapter;
import com.leafymart.Manager.SessionManager;
import com.leafymart.Model.OrderModel;
import com.leafymart.R;
import com.leafymart.Structure.ApiConfig;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView profileName, profileEmail;
    private ImageView profileImage;
    private RecyclerView orderRecyclerView;
    private List<OrderModel> orderList;
    private OrderAdapter orderAdapter;

    private static final long POLL_INTERVAL = 5000; //  5seconds
    private Handler handler = new Handler();
    private Runnable ordersChecker;
    private int userId;
    private long lastUpdateTime = 0;


    // TODO: Replace with real logged-in user ID or pass it as argument

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profileImage = view.findViewById(R.id.profile_image);
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView);

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, getContext());
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderRecyclerView.setAdapter(orderAdapter);

        // Get user ID from session manager
         userId = SessionManager.getInstance(requireContext()).getUserId();
        fetchProfileData(userId);

        return view;
    }

    private void fetchProfileData(int userId) {
        String url = ApiConfig.BASE_URL+ "/profile/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONObject user = response.getJSONObject("user");
                            String name = user.getString("name");
                            String email = user.getString("email");
                            String profileImageUrl = user.getString("profile_image_url");

                            profileName.setText(name);
                            profileEmail.setText(email);

                            Picasso.get()
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.add_face)
                                    .error(R.drawable.baseline_star_16)
                                    .into(profileImage);


                            fetchOrders(userId);

                        } else {
                            profileName.setText("User not found");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                    profileName.setText("Error loading profile");
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }
    private void fetchOrders(int userId) {
        String url = ApiConfig.BASE_URL + "/orders/user/" + userId + "?last_update=" + lastUpdateTime;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Check if response is null
                        if (response == null) {
                            showError("Empty server response");
                            return;
                        }

                        // Handle error responses
                        if (response.has("error")) {
                            showError(response.getString("error"));
                            return;
                        }

                        // Check status code
                        int status = response.optInt("status", 200);
                        if (status != 200) {
                            showError("Server error: " + status);
                            return;
                        }

                        // Handle empty orders case
                        if (!response.has("orders")) {
                            updateOrderList(new ArrayList<>());
                            return;
                        }

                        JSONArray ordersArray = response.getJSONArray("orders");
                        List<OrderModel> newOrders = new ArrayList<>();

                        // Safely parse orders
                        if (ordersArray != null) {
                            for (int i = 0; i < ordersArray.length(); i++) {
                                JSONObject o = ordersArray.getJSONObject(i);
                                newOrders.add(new OrderModel(
                                        o.getInt("order_id"),
                                        o.getString("order_date"),
                                        o.getString("status")
                                ));
                            }
                        }

                        updateOrderList(newOrders);

                        // Update last update time
                        if (response.has("last_updated")) {
                            lastUpdateTime = response.getLong("last_updated");
                        } else {
                            lastUpdateTime = System.currentTimeMillis();
                        }

                    } catch (JSONException e) {
                        Log.e("ProfileFragment", "Order parse error", e);
                        showError("Error parsing order data");
                    }
                },
                error -> {
                    Log.e("ProfileFragment", "Order fetch error", error);
                    showError("Network error, retrying...");
                    handler.postDelayed(() -> fetchOrders(userId), POLL_INTERVAL);
                }
        ) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    // Handle empty responses
                    if (response.data == null || response.data.length == 0) {
                        return Response.success(new JSONObject("{\"status\":200,\"orders\":[]}"),
                                HttpHeaderParser.parseCacheHeaders(response));
                    }
                    return super.parseNetworkResponse(response);
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        // Set retry policy
        req.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // 10 sec timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(requireContext()).add(req);
    }

    private List<OrderModel> parseOrders(JSONArray ordersArray) throws JSONException {
        List<OrderModel> orders = new ArrayList<>();
        for (int i = 0; i < ordersArray.length(); i++) {
            JSONObject o = ordersArray.getJSONObject(i);
            orders.add(new OrderModel(
                    o.getInt("order_id"),
                    o.getString("order_date"),
                    o.getString("status")
            ));
        }
        return orders;
    }

    private boolean hasShownEmptyToast = false;

    private void updateOrderList(List<OrderModel> newOrders) {
        if (newOrders.isEmpty()) {
            if (!hasShownEmptyToast) {
                if (orderList.isEmpty()) {
                    Toast.makeText(getContext(), "order list empty", Toast.LENGTH_SHORT).show();
                    hasShownEmptyToast = true;  // mark toast shown
                }
            }
            orderList.clear();
            orderAdapter.notifyDataSetChanged();
            return;
        }



        // Update existing orders
        for (OrderModel newOrder : newOrders) {
            boolean found = false;
            for (int i = 0; i < orderList.size(); i++) {
                OrderModel existing = orderList.get(i);
                if (existing.getOrderId() == newOrder.getOrderId()) {
                    if (!existing.getStatus().equals(newOrder.getStatus())) {
                        // Status changed - update
                        orderList.set(i, newOrder);
                        orderAdapter.notifyItemChanged(i);
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                // New order - add
                orderList.add(newOrder);
                orderAdapter.notifyItemInserted(orderList.size() - 1);
            }
        }

        // Remove deleted orders
        for (int i = orderList.size() - 1; i >= 0; i--) {
            boolean exists = false;
            for (OrderModel newOrder : newOrders) {
                if (orderList.get(i).getOrderId() == newOrder.getOrderId()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                orderList.remove(i);
                orderAdapter.notifyItemRemoved(i);
            }
        }
    }



    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        lastUpdateTime = 0;
        startOrdersPolling();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopOrdersPolling();
    }

    private void startOrdersPolling() {
        // Clear any existing callbacks
        stopOrdersPolling();

        // Initial fetch
        fetchOrders(userId);

        // Schedule periodic updates
        ordersChecker = new Runnable() {
            @Override
            public void run() {
                fetchOrders(userId);
                handler.postDelayed(this, POLL_INTERVAL);
            }
        };
        handler.postDelayed(ordersChecker, POLL_INTERVAL);
    }

    private void stopOrdersPolling() {
        handler.removeCallbacksAndMessages(null); // Clear all callbacks
    }

}
