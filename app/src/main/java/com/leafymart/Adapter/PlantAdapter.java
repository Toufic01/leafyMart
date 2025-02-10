package com.leafymart.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leafymart.R;
import com.leafymart.DataClass.Plant;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private Context context;
    private List<Plant> plants;

    public PlantAdapter(Context context, List<Plant> plants) {
        this.context = context;
        this.plants = plants;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.trending_items, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plants.get(position);

        holder.trending_plants_IV.setImageResource(plant.getPlantImage());
        holder.trending_plants_name.setText(plant.getPlantName());
        holder.trending_plants_value.setText(plant.getPlantValue());
        holder.trending_plants_rating.setText(plant.getPlantRating());
        holder.trending_plants_people_rates.setText(plant.getPlantPeopleRates());
        holder.trending_plants_sold.setText(plant.getPlantSold());
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    public class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView trending_plants_IV;
        TextView trending_plants_name;
        TextView trending_plants_value;
        TextView trending_plants_rating;
        TextView trending_plants_people_rates;
        TextView trending_plants_sold;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            trending_plants_IV = itemView.findViewById(R.id.trending_plants_IV);
            trending_plants_name = itemView.findViewById(R.id.trending_plants_name);
            trending_plants_value = itemView.findViewById(R.id.trending_plants_value);
            trending_plants_rating = itemView.findViewById(R.id.trending_plants_rating);
            trending_plants_people_rates = itemView.findViewById(R.id.trending_plants_people_rates);
            trending_plants_sold = itemView.findViewById(R.id.trending_plants_sold);
        }
    }
}