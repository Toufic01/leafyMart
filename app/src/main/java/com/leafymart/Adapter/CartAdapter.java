package com.leafymart.Adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.leafymart.Manager.CartManager;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Structure.CartHelper;
import com.leafymart.Structure.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<PlantModel> cartList;
    private final Context context;
    private final int userId;

    public CartAdapter(Context context, List<PlantModel> cartList, int userId) {
        this.context = context;
        this.cartList = cartList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PlantModel item = cartList.get(position);

        // Bind basic item info
        holder.name.setText(item.getName());
        holder.price.setText(String.format("৳%.2f", item.getPrice()));

        // Load product image with Glide
        loadProductImage(item, holder.imageView);

        // Quantity controls
        int quantity = item.getQuantity();
        holder.quantityText.setText(String.valueOf(quantity));
        holder.quantitySeekBar.setProgress(quantity);

        holder.quantitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    seekBar.setProgress(1);
                    return;
                }
                holder.quantityText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newQty = seekBar.getProgress();
                updateCartQuantity(item, newQty, position);
            }
        });

        // Delete button
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Item")
                    .setMessage("Are you sure you want to remove this item?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        removeItem(item.getCartItemId(), position);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void removeItem(int cartItemId, int position) {
        CartHelper.deleteCartItem(context, cartItemId, userId,
                new CartHelper.CartOperationCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Remove item from list and update UI
                        cartList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error, String detailedError) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();

                    }

                }
        );
    }

    private void loadProductImage(PlantModel item, ImageView imageView) {
        String imageUrl = item.getImageUrl();

        if (imageUrl == null || imageUrl.isEmpty()) {
            // If no URL, try to fetch product details
            fetchProductImage(item.getId(), imageView);
            return;
        }

        // Add cache busting parameter
        String finalUrl = imageUrl.contains("?") ?
                imageUrl + "&t=" + System.currentTimeMillis() :
                imageUrl + "?t=" + System.currentTimeMillis();

        Log.d("ImageLoad", "Loading image from: " + finalUrl);

        Glide.with(context)
                .load(finalUrl)
                .placeholder(R.drawable.baseline_star_16)
                .error(R.drawable.baseline_notifications_24)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e("ImageError", "Failed to load image: " + finalUrl, e);
                        // Try fetching from product details if cart image fails
                        fetchProductImage(item.getId(), imageView);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d("ImageSuccess", "Image loaded successfully: " + finalUrl);
                        return false;
                    }
                })
                .into(imageView);
    }

    private void fetchProductImage(int productId, ImageView imageView) {
        String url = ApiConfig.BASE_URL + "/products/" + productId;
        Log.d("FetchProduct", "Fetching product details: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        String imageUrl = response.getString("image_url");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(imageUrl)
                                    .into(imageView);
                        } else {
                            imageView.setImageResource(R.drawable.gardening_tools);
                        }
                    } catch (JSONException e) {
                        Log.e("FetchProduct", "Error parsing product response", e);
                        imageView.setImageResource(R.drawable.leafymart);
                    }
                },
                error -> {
                    Log.e("FetchProduct", "Failed to fetch product", error);
                    imageView.setImageResource(R.drawable.indoor_plant);
                }
        );

        Volley.newRequestQueue(context).add(request);
    }

//    public static void deleteCartItem(Context context, int cartItemId, int userId,
//                                      CartHelper.CartOperationCallback callback) {
//        String url = ApiConfig.BASE_URL + "/cart/" + cartItemId + "?user_id=" + userId;
//
//        JsonObjectRequest request = new JsonObjectRequest(
//                Request.Method.DELETE,
//                url,
//                null,
//                response -> {
//                    try {
//                        if (response.getBoolean("success")) {
//                            callback.onSuccess(response);
//                        } else {
//                            callback.onError(response.getString("message"));
//                        }
//                    } catch (Exception e) {
//                        callback.onError("Invalid response format");
//                    }
//                },
//                error -> {
//                    callback.onError(getErrorMessage(error));
//                }
//        ) {
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
//                return headers;
//            }
//        };
//
//        Volley.newRequestQueue(context).add(request);
//    }
    private void updateCartQuantity(PlantModel item, int newQuantity, int position) {
        int oldQuantity = item.getQuantity();
        item.setQuantity(newQuantity);
        notifyItemChanged(position);

        CartManager.getInstance(context).updateQuantity(
                userId,
                item.getId(),
                newQuantity,
                new CartManager.CartCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("CartUpdate", "Quantity updated successfully");
                    }

                    @Override
                    public void onFailure(String error) {
                        item.setQuantity(oldQuantity);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Update failed: " + error, Toast.LENGTH_SHORT).show();
                        Log.e("CartUpdate", "Error updating quantity", new Exception(error));
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name, price, quantityText;
        SeekBar quantitySeekBar;
        ImageButton deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cart_item_image);
            name = itemView.findViewById(R.id.cart_item_name);
            price = itemView.findViewById(R.id.cart_item_price);
            quantityText = itemView.findViewById(R.id.cart_item_quantity_text);
            quantitySeekBar = itemView.findViewById(R.id.cart_item_quantity_seekbar);
            deleteButton = itemView.findViewById(R.id.cart_item_delete);
        }
    }
}