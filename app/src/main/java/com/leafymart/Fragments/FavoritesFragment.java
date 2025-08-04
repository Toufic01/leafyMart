package com.leafymart.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leafymart.Adapter.PlantAdapter;
import com.leafymart.Manager.FavoriteManager;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private TextView emptyStateText;
    private FavoriteManager favoriteManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        favoriteManager = FavoriteManager.get(requireContext());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Initialize adapter
        adapter = new PlantAdapter(requireContext(), favoriteManager.getFavoriteProducts(),
                PlantAdapter.VIEW_TYPE_FAVORITE);
        adapter.setOnFavoriteChangeListener(this::refreshFavorites);
        recyclerView.setAdapter(adapter);

        // Set up empty state
        updateEmptyState();

        // Listen for changes
        favoriteManager.setOnChangeListener(this::refreshFavorites);
    }

    private void refreshFavorites() {
        adapter.update(favoriteManager.getFavoriteProducts());
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (favoriteManager.getFavoriteProducts().isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        favoriteManager.pullFromServer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        favoriteManager.setOnChangeListener(null);
    }
}