package com.leafymart.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;

import java.util.ArrayList;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private Context context;
    private ArrayList<PlantModel> items;
    private QuantityChangeListener listener;

    public interface QuantityChangeListener {
        void onQuantityChanged();
    }

    public InvoiceAdapter(Context context, ArrayList<PlantModel> items, QuantityChangeListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice_product, parent, false);
        return new InvoiceViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        PlantModel product = items.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText("৳" + product.getPrice());
        holder.productRating.setText("Rating: " + product.getRating());
        holder.quantity.setText(String.valueOf(product.getQuantity()));

        Glide.with(context).load(product.getImageUrl()).into(holder.productImage);

        holder.btnMinus.setOnClickListener(v -> {
            int q = product.getQuantity();
            if (q > 1) {
                product.setQuantity(q - 1);
                notifyItemChanged(position);
                listener.onQuantityChanged();
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            product.setQuantity(product.getQuantity() + 1);
            notifyItemChanged(position);
            listener.onQuantityChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productRating, quantity;
        ImageView productImage;
        Button btnMinus, btnPlus;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productRating = itemView.findViewById(R.id.productRating);
            productImage = itemView.findViewById(R.id.productImage);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            quantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
