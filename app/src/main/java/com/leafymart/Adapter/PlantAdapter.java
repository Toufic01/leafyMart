package com.leafymart.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.leafymart.Dialogbox.ProductDetailsDialog;
import com.leafymart.Manager.FavoriteManager;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    public interface OnFavoriteChangeListener {
        void onFavoritesChanged();
    }

    public static final int VIEW_TYPE_TRENDING = 0;
    public static final int VIEW_TYPE_FAVORITE = 1;

    private OnFavoriteChangeListener favListener;
    public final Context ctx;
    private List<PlantModel> list;
    private final int viewType;

    public PlantAdapter(Context c, List<PlantModel> l, int viewType) {
        ctx = c;
        list = l;
        this.viewType = viewType;
    }

    public void setOnFavoriteChangeListener(OnFavoriteChangeListener listener) {
        this.favListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(
                viewType == VIEW_TYPE_FAVORITE ? R.layout.favorite_item : R.layout.trending_items,
                parent, false);
        return new PlantViewHolder(view, viewType);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantModel plant = list.get(position);
        if (plant == null) return;

        // Bind data to views
        holder.name.setText(plant.getName());
        holder.price.setText("৳" + plant.getPrice());

        // Load image
        Glide.with(ctx)
                .load(plant.getImageUrl())
                .into(holder.img);

        // Set rating and sold if they exist
        if (holder.rating != null) {
            holder.rating.setText(String.valueOf(plant.getRating()));
        }
        if (holder.sold != null) {
            holder.sold.setText(plant.getSold() + " sold");
        }

        // Handle favorite icon
        if (holder.favorite != null) {
            updateFavoriteIcon(holder.favorite, plant.getId());
            holder.favorite.setOnClickListener(v -> toggleFavorite(holder, plant));
        }

        // Handle delete button (only in favorites view)
        if (holder.delete != null) {
            holder.delete.setOnClickListener(v -> deleteFavorite(holder, plant));
        }

        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            if (ctx instanceof FragmentActivity) {
                ProductDetailsDialog dialog = new ProductDetailsDialog((FragmentActivity) ctx, plant);
                dialog.show();
            }
        });
    }

    private void toggleFavorite(PlantViewHolder holder, PlantModel plant) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        FavoriteManager favoriteManager = FavoriteManager.get(ctx);
        boolean isNowFavorite = !favoriteManager.isFav(plant.getId());
        updateFavoriteIcon(holder.favorite, plant.getId(), isNowFavorite);

        if (isNowFavorite) {
            favoriteManager.add(plant);
        } else {
            favoriteManager.remove(plant);
            if (viewType == VIEW_TYPE_FAVORITE) {
                list.remove(pos);
                notifyItemRemoved(pos);
            }
        }

        if (favListener != null) {
            favListener.onFavoritesChanged();
        }
    }
    private void deleteFavorite(PlantViewHolder holder, PlantModel plant) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        FavoriteManager.get(ctx).remove(plant);
        list.remove(pos);
        notifyItemRemoved(pos);

        if (favListener != null) {
            favListener.onFavoritesChanged();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void update(List<PlantModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    private void updateFavoriteIcon(ImageView heartIcon, int productId) {
        if (heartIcon == null) return;
        heartIcon.setImageResource(
                FavoriteManager.get(ctx).isFav(productId)
                        ? R.drawable.image_removebg_preview
                        : R.drawable.baseline_favorite_border_24
        );
    }

    private void updateFavoriteIcon(ImageView heartIcon, int productId, boolean isFavorite) {
        if (heartIcon == null) return;
        heartIcon.setImageResource(
                isFavorite
                        ? R.drawable.image_removebg_preview
                        : R.drawable.baseline_favorite_border_24
        );
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView img, favorite;
        Button delete;
        TextView name, price, rating, sold;

        PlantViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            if (viewType == VIEW_TYPE_FAVORITE) {
                img = itemView.findViewById(R.id.favorite_item_image);
                favorite = itemView.findViewById(R.id.favorites);
                delete = itemView.findViewById(R.id.favorite_item_delete);
                name = itemView.findViewById(R.id.favorite_item_name);
                price = itemView.findViewById(R.id.favorite_item_price);
                rating = itemView.findViewById(R.id.favorite_plants_rating);
                sold = itemView.findViewById(R.id.favorite_plants_sold);
            } else {
                img = itemView.findViewById(R.id.trending_plants_IV);
                favorite = itemView.findViewById(R.id.favorites);
                delete = null;
                name = itemView.findViewById(R.id.trending_plants_name);
                price = itemView.findViewById(R.id.trending_plants_value);
                rating = itemView.findViewById(R.id.trending_plants_rating);
                sold = itemView.findViewById(R.id.trending_plants_sold);
            }
        }
    }
}