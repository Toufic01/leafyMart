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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class categories_viewAll extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private String categoryName;

    private RecyclerView recyclerView;
    private PlantAdapter plantAdapter;
    private final ArrayList<PlantModel> plantList = new ArrayList<>();

    public categories_viewAll() {
        // Required empty constructor
    }

    public static categories_viewAll newInstance(String category) {
        categories_viewAll fragment = new categories_viewAll();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories_view_all, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        if (getArguments() != null) {
            categoryName = getArguments().getString(ARG_CATEGORY);
        }

        recyclerView = v.findViewById(R.id.trending_categories_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        plantAdapter = new PlantAdapter(requireContext(), plantList, PlantAdapter.VIEW_TYPE_TRENDING);
        recyclerView.setAdapter(plantAdapter);

        fetchCategoryProducts(categoryName);
    }

    private void fetchCategoryProducts(String categoryName) {
        String url = ApiConfig.BASE_URL + "/products";

        if (categoryName != null && !categoryName.isEmpty()) {
            url += "?category=" + categoryName;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("category", categoryName);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    plantList.clear();
                    parseArray(response);
                    plantAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(getContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public byte[] getBody() {
                return requestBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        MySingleton.get(requireContext()).add(request);
    }

    private void parseArray(JSONArray arr) {
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                plantList.add(new PlantModel(
                        o.optInt("id"),
                        o.optString("name"),
                        o.optDouble("price"),
                        o.optDouble("rating"),
                        o.optInt("sold"),
                        o.optInt("sold"),
                        o.optString("image_url", null)
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}