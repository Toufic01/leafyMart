package com.leafymart.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.leafymart.Adapter.PlantAdapter;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Structure.MySingleton;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class View_Tranding extends Fragment {

    private PlantAdapter plantAdapter;
    private final List<PlantModel> plants = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view__tranding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.all_products_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Initialize adapter with empty list
        plantAdapter = new PlantAdapter(requireContext(), plants, PlantAdapter.VIEW_TYPE_TRENDING);
        recyclerView.setAdapter(plantAdapter);

        // Fetch products
        fetchAllProducts();
    }

    private void fetchAllProducts() {
        String url = ApiConfig.BASE_URL + "/products";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    plants.clear();
                    parseJsonArray(response);
                    plantAdapter.notifyDataSetChanged();

                    // Check if data was loaded
                    if (plants.isEmpty()) {
                        Toast.makeText(getContext(), "No products found", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Error loading products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                });

        MySingleton.get(requireContext()).add(request);
    }

    private void parseJsonArray(JSONArray array) {
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String imageUrl = obj.optString("image_url");

                // Handle image URL
                if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.equalsIgnoreCase("null")) {
                    imageUrl = null;
                } else if (!imageUrl.startsWith("http")) {
                    imageUrl = ApiConfig.BASE_URL + "/" + imageUrl;
                }

                PlantModel plant = new PlantModel(
                        obj.getInt("id"),
                        obj.getString("name"),
                        obj.getDouble("price"),
                        obj.getDouble("rating"),
                        obj.getInt("sold"),
                        obj.getInt("sold"),
                        imageUrl
                );
                plants.add(plant);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing products", Toast.LENGTH_SHORT).show();
        }
    }
}