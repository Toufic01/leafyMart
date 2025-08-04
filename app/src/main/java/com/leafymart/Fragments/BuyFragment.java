package com.leafymart.Fragments;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leafymart.Activities.BkashPaymentActivity;
import com.leafymart.Adapter.InvoiceAdapter;
import com.leafymart.Manager.SessionManager;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuyFragment extends Fragment {

    private static final String ARG_SINGLE_PRODUCT = "singleProduct";
    private static final String ARG_PRODUCT_LIST = "productList";

    private ArrayList<PlantModel> productList;
    private TextView productTotal;
    private Button confirmButton, downloadInvoiceButton;
    private RecyclerView recyclerView;

    public static BuyFragment newInstance(PlantModel product) {
        BuyFragment fragment = new BuyFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SINGLE_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    public static BuyFragment newInstance(ArrayList<PlantModel> products) {
        BuyFragment fragment = new BuyFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT_LIST, products);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productList = new ArrayList<>();
        if (getArguments() != null) {
            PlantModel singleProduct = (PlantModel) getArguments().getSerializable(ARG_SINGLE_PRODUCT);
            ArrayList<PlantModel> list = (ArrayList<PlantModel>) getArguments().getSerializable(ARG_PRODUCT_LIST);
            if (list != null) {
                productList = list;
            } else if (singleProduct != null) {
                productList.add(singleProduct);
            }
        }

        if (productList.isEmpty()) {
            Toast.makeText(getContext(), "No product data found", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy, container, false);

        productTotal = view.findViewById(R.id.productTotal);
        confirmButton = view.findViewById(R.id.confirmButton);
        downloadInvoiceButton = view.findViewById(R.id.downloadInvoiceButton);
        recyclerView = view.findViewById(R.id.recyclerViewInvoice);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        InvoiceAdapter adapter = new InvoiceAdapter(getContext(), productList, this::updateTotal);
        recyclerView.setAdapter(adapter);


        updateTotal();

        confirmButton.setOnClickListener(v -> placeOrder());
        downloadInvoiceButton.setOnClickListener(v -> generateInvoicePdf());

        return view;
    }

    private void updateTotal() {
        double total = 0;
        for (PlantModel p : productList) {
            total += p.getPrice() * p.getQuantity();
        }
        productTotal.setText("Total: ৳" + total);
    }


    private void placeOrder() {
        int userId = SessionManager.getInstance(getContext()).getUserId();
        if (userId == -1) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        List<PlantModel> validProducts = new ArrayList<>();
        for (PlantModel plant : productList) {
            if (plant.getQuantity() > 0) {
                validProducts.add(plant);
            }
        }

        if (validProducts.isEmpty()) {
            Toast.makeText(getContext(), "Please add quantity for at least one product", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject orderData = new JSONObject();

        try {
            orderData.put("user_id", userId);

            // Always send products as an array (even if one product)
            JSONArray productsArray = new JSONArray();
            for (PlantModel plant : validProducts) {
                JSONObject productObj = new JSONObject();
                productObj.put("product_id", plant.getId());
                productObj.put("quantity", plant.getQuantity());
                productObj.put("unit_price", plant.getPrice());  // <-- IMPORTANT: send unit_price, NOT total_price
                productsArray.put(productObj);
            }
            orderData.put("products", productsArray);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing order data", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("OrderData", orderData.toString());

        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiConfig.BASE_URL + "/orders",
                orderData,
                response -> {
                   // generateInvoicePdf();

                    try {
                        int orderId = response.getInt("order_id");

                        Intent intent = new Intent(getContext(), BkashPaymentActivity.class);
                        intent.putExtra("order_id", orderId);
                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error processing order response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = "Failed to place order";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg += ": " + new String(error.networkResponse.data);
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                });
        queue.add(request);
    }






//    private void placeOrder() {
//        int userId = SessionManager.getInstance(getContext()).getUserId();
//        if (userId == -1) {
//            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        List<PlantModel> validProducts = new ArrayList<>();
//        for (PlantModel plant : productList) {
//            if (plant.getQuantity() > 0) {
//                validProducts.add(plant);
//            }
//        }
//
//        if (validProducts.isEmpty()) {
//            Toast.makeText(getContext(), "Please add quantity for at least one product", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        JSONObject orderData = new JSONObject();
//
//        try {
//            orderData.put("user_id", userId);
//
//            if (validProducts.size() == 1) {
//                // Single product order
//                PlantModel plant = validProducts.get(0);
//                orderData.put("product_id", plant.getId());
//                orderData.put("quantity", plant.getQuantity());
//                orderData.put("total_price", plant.getQuantity() * plant.getPrice());
//            } else {
//                // Multiple products order
//                JSONArray productsArray = new JSONArray();
//                for (PlantModel plant : validProducts) {
//                    JSONObject productObj = new JSONObject();
//                    productObj.put("product_id", plant.getId());
//                    productObj.put("quantity", plant.getQuantity());
//                    productObj.put("total_price", plant.getQuantity() * plant.getPrice());
//                    productsArray.put(productObj);
//                }
//                orderData.put("products", productsArray);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Toast.makeText(getContext(), "Error preparing order data", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Log.d("OrderData", orderData.toString());
//
//        RequestQueue queue = Volley.newRequestQueue(getContext());
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiConfig.BASE_URL + "/orders",
//                orderData,
//                response -> {
//                    // Success: generate invoice and go to payment
//                    generateInvoicePdf();
//
//
//                    try {
//                        // Extract order ID from response
//                        int orderId = response.getInt("order_id");
//
//                        // Start payment activity with order ID
//                        Intent intent = new Intent(getContext(), BkashPaymentActivity.class);
//                        intent.putExtra("order_id", orderId);
//                        startActivity(intent);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Toast.makeText(getContext(), "Error processing order response", Toast.LENGTH_SHORT).show();
//                    }
//
//
//                },
//                error -> {
//                    String errorMsg = "Failed to place order";
//                    if (error.networkResponse != null && error.networkResponse.data != null) {
//                        errorMsg += ": " + new String(error.networkResponse.data);
//                    }
//                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
//                });
//        queue.add(request);
//    }



    private void generateInvoicePdf() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create(); // bigger page for better layout
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int x = 40;
        int y = 60;

        // Title background rectangle (blue)
        paint.setColor(Color.parseColor("#2196F3"));
        canvas.drawRect(0, 0, canvas.getWidth(), 100, paint);

        // Invoice title text (white)
        paint.setColor(Color.WHITE);
        paint.setTextSize(28f);
        paint.setFakeBoldText(true);
        canvas.drawText("LeafyMart Invoice", x, 65, paint);

        // Switch to black paint for normal text below title
        paint.setColor(Color.BLACK);
        paint.setTextSize(18f);
        paint.setFakeBoldText(false);

        y = 120;  // start drawing below the blue title bar

        // Customer name
        String userName = SessionManager.getInstance(getContext()).getUserName();
        if (userName == null || userName.isEmpty()) userName = "Guest User";
        canvas.drawText("Customer: " + userName, x, y, paint);

        // Date
        y += 25;
        String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        canvas.drawText("Date: " + formattedDate, x, y, paint);

        // Draw table header background (light gray)
        y += 40;
        paint.setColor(Color.parseColor("#F5F5F5"));
        canvas.drawRect(x - 10, y - 20, canvas.getWidth() - 40, y + 20, paint);

        // Draw table header text (black, bold)
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Product", x, y, paint);
        canvas.drawText("Qty", x + 250, y, paint);
        canvas.drawText("Price", x + 320, y, paint);
        canvas.drawText("Subtotal", x + 420, y, paint);

        // Draw table rows
        paint.setFakeBoldText(false);
        paint.setTextSize(16f);
        y += 30;

        double total = 0;
        for (PlantModel product : productList) {
            double subtotal = product.getQuantity() * product.getPrice();

            canvas.drawText(product.getName(), x, y, paint);
            canvas.drawText(String.valueOf(product.getQuantity()), x + 250, y, paint);
            canvas.drawText("৳" + product.getPrice(), x + 320, y, paint);
            canvas.drawText("৳" + subtotal, x + 420, y, paint);

            y += 25;
            total += subtotal;
        }

        // Draw line separator
        paint.setStrokeWidth(2);
        canvas.drawLine(x, y + 10, canvas.getWidth() - 40, y + 10, paint);
        y += 40;

        // Draw total amount (green, bold)
        paint.setTextSize(20f);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#4CAF50"));
        canvas.drawText("Total Amount: ৳" + total, x, y, paint);

        document.finishPage(page);

        // Save PDF file to Downloads/LeafyMarts folder
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LeafyMarts");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "Invoice_" + System.currentTimeMillis() + ".pdf");

        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(getContext(), "Invoice saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error saving invoice", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        document.close();
    }


}
