package com.leafymart.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leafymart.R;
import com.leafymart.Structure.ApiConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class TrackProductActivity extends AppCompatActivity {

    private TextView trackingNumber, currentStatus, statusDescription;
    private View stage1, stage2, stage3, stage4;
    private View line1, line2, line3;
    private ProgressBar progressBar;
    private String orderId;

    private static final long POLL_INTERVAL = 10000; // 10 seconds
    private Handler handler = new Handler();
    private Runnable statusChecker;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_product);

        initializeViews();
        setupOrderTracking();
    }

    private void initializeViews() {
        trackingNumber = findViewById(R.id.tracking_number);
        currentStatus = findViewById(R.id.current_status);
        statusDescription = findViewById(R.id.status_description);

        stage1 = findViewById(R.id.stage1);
        stage2 = findViewById(R.id.stage2);
        stage3 = findViewById(R.id.stage3);
        stage4 = findViewById(R.id.stage4);

        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);

        progressBar = findViewById(R.id.progressBar);
    }

    private void setupOrderTracking() {
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey("order_id")) {
            handleInvalidOrder();
            return;
        }

        Object orderIdObj = extras.get("order_id");
        if (orderIdObj instanceof String) {
            orderId = (String) orderIdObj;
        } else if (orderIdObj instanceof Integer) {
            orderId = String.valueOf((Integer) orderIdObj);
        } else if (orderIdObj instanceof Long) {
            orderId = String.valueOf((Long) orderIdObj);
        } else {
            handleInvalidOrder();
            return;
        }

        if (orderId.isEmpty() || orderId.equals("0") || orderId.equals("-1")) {
            handleInvalidOrder();
            return;
        }

        trackingNumber.setText(getString(R.string.order_number, orderId));
        fetchOrderStatus();
    }

    private void handleInvalidOrder() {
        Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void fetchOrderStatus() {
        progressBar.setVisibility(View.VISIBLE);

        String url = ApiConfig.BASE_URL + "/order/tracking/" + orderId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.has("success") && !response.getBoolean("success")) {
                            String errorMsg = response.getString("message");
                            showErrorAndFinish(errorMsg);
                            return;
                        }
                        updateUI(response);
                    } catch (JSONException e) {
                        showErrorAndFinish("Error parsing order data");
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    showErrorAndFinish(getString(R.string.network_error));
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
    private void updateUI(JSONObject response) throws JSONException {
        String status = response.getString("status");
        String description = response.getString("description");

        currentStatus.setText(getString(R.string.current_status, status));
        statusDescription.setText(description);

        updateProgressVisuals(status);
    }

    private void updateProgressVisuals(String status) {
        resetStages();

        switch (status) {
            case "delivered":
                markStageComplete(stage4);
                markLineComplete(line3);
                // Fall through
            case "delivery":
                markStageComplete(stage3);
                markLineComplete(line2);
                // Fall through
            case "in_station":
                markStageComplete(stage2);
                markLineComplete(line1);
                // Fall through
            case "processing":
                markStageComplete(stage1);
                break;
            default:
                // Handle unknown status
                break;
        }
    }

    private void resetStages() {
        int greyColor = ContextCompat.getColor(this, R.color.grey);

        stage1.setBackgroundResource(R.drawable.circle_grey);
        stage2.setBackgroundResource(R.drawable.circle_grey);
        stage3.setBackgroundResource(R.drawable.circle_grey);
        stage4.setBackgroundResource(R.drawable.circle_grey);

        line1.setBackgroundColor(greyColor);
        line2.setBackgroundColor(greyColor);
        line3.setBackgroundColor(greyColor);
    }

    private void markStageComplete(View stage) {
        stage.setBackgroundResource(R.drawable.circle_blue);
    }

    private void markLineComplete(View line) {
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startStatusPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopStatusPolling();
    }

    private void startStatusPolling() {
        statusChecker = new Runnable() {
            @Override
            public void run() {
                fetchOrderStatus();
                handler.postDelayed(this, POLL_INTERVAL);
            }
        };
        handler.postDelayed(statusChecker, POLL_INTERVAL);
    }

    private void stopStatusPolling() {
        if (statusChecker != null) {
            handler.removeCallbacks(statusChecker);
        }
    }


}