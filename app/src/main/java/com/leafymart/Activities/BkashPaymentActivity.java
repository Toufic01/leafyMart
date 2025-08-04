package com.leafymart.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.leafymart.Manager.SessionManager;
import com.leafymart.R;

public class BkashPaymentActivity extends AppCompatActivity {

    private Button btnBkashPay, btnFreePay, btnTrackProduct;
    private int orderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bkash_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBkashPay = findViewById(R.id.btnBkashPay);
        btnFreePay = findViewById(R.id.btnFreePay);
        btnTrackProduct = findViewById(R.id.btnTrackProduct);

        // Get order_id passed from previous activity
        orderId = getIntent().getIntExtra("order_id", -1);

        btnBkashPay.setOnClickListener(v -> {
            if(orderId == -1) {
                Toast.makeText(this, "Order ID not available", Toast.LENGTH_SHORT).show();
                return;
            }
            startBkashPayment();
        });

        btnFreePay.setOnClickListener(v -> {
            if(orderId == -1) {
                Toast.makeText(this, "Order ID not available", Toast.LENGTH_SHORT).show();
                return;
            }
            processFreePayment();
        });

        btnTrackProduct.setOnClickListener(v -> {
            if(orderId == -1) {
                Toast.makeText(BkashPaymentActivity.this, "Order ID not available", Toast.LENGTH_SHORT).show();
                return;
            }
            int userId = SessionManager.getInstance(this).getUserId();
            if (userId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, TrackProductActivity.class);
            intent.putExtra("order_id", orderId);
            intent.putExtra("user_id", userId);  // ✅ send user_id to backend
            startActivity(intent);
        });
    }

    private void startBkashPayment() {
        // Your existing Bkash payment implementation here
        Toast.makeText(this, "Starting Bkash payment for order " + orderId, Toast.LENGTH_SHORT).show();
    }

    private void processFreePayment() {
        // Your logic for free payment here
        Toast.makeText(this, "Order placed successfully with Free Payment for order " + orderId, Toast.LENGTH_LONG).show();
        finish();
    }
}
