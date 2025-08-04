package com.leafymart.Dialogbox;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.leafymart.Fragments.BuyFragment;
import com.leafymart.Manager.CartManager;
import com.leafymart.Manager.FavoriteManager;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;

public class ProductDetailsDialog extends AlertDialog {

    private final PlantModel product;
    private final FragmentActivity activity;

    public ProductDetailsDialog(FragmentActivity activity, PlantModel product) {
        super(activity);
        this.activity = activity;
        this.product = product;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDialogLayout();
    }

    private void setupDialogLayout() {
        int padding = dpToPx(16);
        int imageHeight = dpToPx(180);

        LinearLayout mainContainer = new LinearLayout(getContext());
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(padding, padding, padding, padding);
        mainContainer.setBackground(createRoundedBackground(Color.WHITE, dpToPx(12)));
        mainContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        mainContainer.setMinimumWidth(dpToPx(300));

        // Product Image
        ImageView productImage = new ImageView(getContext());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                imageHeight);
        imageParams.bottomMargin = dpToPx(12);
        productImage.setLayoutParams(imageParams);
        productImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(getContext()).load(product.getImageUrl()).into(productImage);

        // Product Name
        TextView nameText = new TextView(getContext());
        nameText.setText(product.getName());
        nameText.setTextSize(18f);
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);
        nameText.setTextColor(Color.BLACK);
        nameText.setPadding(0, 0, 0, dpToPx(6));

        // Rating and Sold
        TextView ratingSoldText = new TextView(getContext());
        ratingSoldText.setText(String.format("★ %.1f  |  %d sold", product.getRating(), product.getSold()));
        ratingSoldText.setTextSize(14f);
        ratingSoldText.setTextColor(Color.DKGRAY);
        ratingSoldText.setPadding(0, 0, 0, dpToPx(12));

        // Price
        TextView priceText = new TextView(getContext());
        priceText.setText(String.format("৳%.2f", product.getPrice()));
        priceText.setTextSize(20f);
        priceText.setTypeface(null, android.graphics.Typeface.BOLD);
        priceText.setTextColor(Color.parseColor("#2E7D32"));
        priceText.setPadding(0, 0, 0, dpToPx(16));

        // Add to Cart Button
        android.widget.Button addToCartButton = new android.widget.Button(getContext());
        addToCartButton.setText("Add to Cart");
        addToCartButton.setTextColor(Color.WHITE);
        addToCartButton.setBackgroundColor(Color.parseColor("#FF6F00"));
        addToCartButton.setAllCaps(false);
        addToCartButton.setTextSize(16f);
        addToCartButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(48)
        ));

        addToCartButton.setOnClickListener(v -> {
            int userId = com.leafymart.Manager.SessionManager.getInstance(getContext()).getUserId();
            if (userId == -1) {
                Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            CartManager.getInstance(getContext()).addToCart(userId, product.getId(), 1, new CartManager.CartCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Failed to add to cart: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Buy Now Button
        android.widget.Button buyNowButton = new android.widget.Button(getContext());
        buyNowButton.setText("Buy Now");
        buyNowButton.setTextColor(Color.WHITE);
        buyNowButton.setBackgroundColor(Color.parseColor("#388E3C"));
        buyNowButton.setAllCaps(false);
        buyNowButton.setTextSize(16f);
        LinearLayout.LayoutParams buyBtnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(48));
        buyBtnParams.topMargin = dpToPx(12);
        buyNowButton.setLayoutParams(buyBtnParams);

        buyNowButton.setOnClickListener(v -> {
            BuyFragment fragment = BuyFragment.newInstance(product);
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
            dismiss();
        });

        // Add views in order
        mainContainer.addView(productImage);
        mainContainer.addView(nameText);
        mainContainer.addView(ratingSoldText);
        mainContainer.addView(priceText);
        mainContainer.addView(addToCartButton);
        mainContainer.addView(buyNowButton);  // ✅ THIS LINE WAS MISSING

        setContentView(mainContainer); // ✅ Set content after all views are added
    }

    private GradientDrawable createRoundedBackground(int color, int cornerRadiusPx) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(cornerRadiusPx);
        return shape;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.5f);
        }
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
