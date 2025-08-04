package com.leafymart.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leafymart.Model.PlantModel;
import com.leafymart.R;

import java.util.ArrayList;

public class BuyAdapter extends RecyclerView.Adapter<BuyAdapter.BuyViewHolder> {

    private final Context context;
    private final ArrayList<PlantModel> products;

    public BuyAdapter(Context context, ArrayList<PlantModel> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public BuyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_buy_product, parent, false);
        return new BuyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuyViewHolder holder, int position) {
        PlantModel product = products.get(position);
        holder.name.setText(product.getName());
        holder.price.setText("৳" + String.format("%.2f", product.getPrice()));
        holder.quantity.setText("Qty: " + product.getQuantity());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class BuyViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, quantity;

        public BuyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.buy_product_name);
            price = itemView.findViewById(R.id.buy_product_price);
            quantity = itemView.findViewById(R.id.buy_product_quantity);
        }
    }
}
