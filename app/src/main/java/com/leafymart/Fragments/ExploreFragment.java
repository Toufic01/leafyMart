package com.leafymart.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leafymart.R;
import com.leafymart.Adapter.PlantAdapter;
import com.leafymart.Model.PlantModel;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    RecyclerView recyclerView;
    PlantAdapter plantAdapter;
    List<PlantModel> plants;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.trending_plants_recyclerView);
        // Create data
        plants = new ArrayList<>();
        plants.add(new PlantModel("Plant 1", "৳100", "5.0", "(87)", "161", R.drawable.plant_1));
        plants.add(new PlantModel("Plant 2", "৳150", "4.5", "(52)", "120", R.drawable.plant_2));
        plants.add(new PlantModel("Plant 3", "৳200", "4.8", "(95)", "180", R.drawable.plant_3));
        plants.add(new PlantModel("Plant 4", "৳120", "4.2", "(30)", "90", R.drawable.plant_4));
        plants.add(new PlantModel("Plant 5", "৳80", "4.0", "(12)", "60", R.drawable.plant_5));
        plants.add(new PlantModel("Plant 6", "৳180", "4.7", "(68)", "140", R.drawable.plant_6));

        // Create adapter
        plantAdapter = new PlantAdapter(requireContext(), plants);
        // Set layout manager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        // Set adapter
        recyclerView.setAdapter(plantAdapter);
    }
}