package com.leafymart.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.leafymart.Adapter.ImageSliderAdapter;
import com.leafymart.R;
import com.leafymart.Adapter.PlantAdapter;
import com.leafymart.Model.PlantModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExploreFragment extends Fragment {

    RecyclerView recyclerView;
    PlantAdapter plantAdapter;
    List<PlantModel> plants;

    private ViewPager2 viewPager2;

    Toolbar toolbar;

    TextView categories_view_all,Trending_view_all;


    public ExploreFragment() {
        /// Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /// Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.trending_plants_recyclerView);


        /// set up the toolbar and 3 dot

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        // else part of the toolbar 3 dot work in blow by calling onOption create function




        /// categories view all function work

        categories_view_all = view.findViewById(R.id.categories_viewAll_TV);

        // setting listener for go to view page

        categories_view_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                categories_viewAll fragment = new categories_viewAll();

                getParentFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right, // Entering fragment animation
                                R.anim.slide_out_left, // Exiting fragment animation
                                R.anim.slide_in_left,  // Pop enter animation (when going back)
                                R.anim.slide_out_right // Pop exit animation (when going back)
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });


        /// Create data
        plants = new ArrayList<>();
        plants.add(new PlantModel("Plant 1", "৳100", "5.0", "(87)", "161", R.drawable.plant_1));
        plants.add(new PlantModel("Plant 2", "৳150", "4.5", "(52)", "120", R.drawable.plant_2));
        plants.add(new PlantModel("Plant 3", "৳200", "4.8", "(95)", "180", R.drawable.plant_3));
        plants.add(new PlantModel("Plant 4", "৳120", "4.2", "(30)", "90", R.drawable.plant_4));
        plants.add(new PlantModel("Plant 5", "৳80", "4.0", "(12)", "60", R.drawable.plant_5));
        plants.add(new PlantModel("Plant 6", "৳180", "4.7", "(68)", "140", R.drawable.plant_6));

        /// Create adapter
        plantAdapter = new PlantAdapter(requireContext(), plants);
        // Set layout manager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        // Set adapter
        recyclerView.setAdapter(plantAdapter);


        /// ImageSlider
        viewPager2 = view.findViewById(R.id.imageSlider);

        // List of images
        List<Integer> images = Arrays.asList(
                R.drawable.image_plant,
                R.drawable.image_plant1,
                R.drawable.image_plant2,
                R.drawable.image_plant3,
                R.drawable.image_plant4
        );

        ImageSliderAdapter adapter = new ImageSliderAdapter(requireContext(), images);
        viewPager2.setAdapter(adapter);

        // Automatic sliding of images
        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager2.getCurrentItem();
                int totalItem = adapter.getItemCount();

                // Change the slide
                if (currentItem == totalItem - 1) {
                    viewPager2.setCurrentItem(0, true); // if last item, start from the first
                } else {
                    viewPager2.setCurrentItem(currentItem + 1, true); // otherwise, go to the next item
                }
            }
        };

        // Set the interval (2 seconds)
        final int delay = 2000; // in milliseconds

        // Start the handler to change the slide automatically
        handler.postDelayed(update, delay);

        // To repeat the automatic sliding at the defined time interval
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, delay);
                update.run();
            }
        }, delay);




        /// view Trending all function work

        Trending_view_all = view.findViewById(R.id.trending_plants_viewAll_TV);

        // setting listener for go to view page

        Trending_view_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View_Tranding fragment = new View_Tranding(); // Corrected class name

                getParentFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right, // Entering fragment animation
                                R.anim.slide_out_left, // Exiting fragment animation
                                R.anim.slide_in_left,  // Pop enter animation (when going back)
                                R.anim.slide_out_right // Pop exit animation (when going back)
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();

            }
        });


    }
    /// Inflate the menu (top toolbar 3 dot menu)

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu into the provided Menu object
        inflater.inflate(R.menu.menu_top, menu);
        super.onCreateOptionsMenu(menu, inflater);  // Call the superclass method if needed
    }


    // Handle menu item clicks

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_about) {

            // Handle about action work

            return true;

        }

        if (id == R.id.action_settings) {

            // Handle setting action work

            return true;
        }

        if (id == R.id.action_logout) {

            // Handle logout action work

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}