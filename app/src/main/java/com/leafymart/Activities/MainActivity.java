package com.leafymart.Activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import com.leafymart.Fragments.CartFragment;
import com.leafymart.Fragments.ExploreFragment;
import com.leafymart.Fragments.FavoritesFragment;
import com.leafymart.Fragments.MessagesFragment;
import com.leafymart.Fragments.ProfileFragment;
import com.leafymart.R;
import com.leafymart.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
// for using same theme for light and dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setStatusBarColor(getResources().getColor(R.color.light_grey));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });


        /// set up the toolbar and 3 dot

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set Toolbar as ActionBar

        // else part of the toolbar 3 dot work in blow by calling onOption create function




        /// Replace Fragment work and fragment changing work

        replaceFragment(new ExploreFragment());
        binding.bottomNavView.setBackground(null);
        binding.bottomNavView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_explore: {
                    replaceFragment(new ExploreFragment());
                    break;
                }
                case R.id.bottom_favorites: {
                    replaceFragment(new FavoritesFragment());
                    break;
                }
                case R.id.bottom_cart: {
                    replaceFragment(new CartFragment());
                    break;
                }
                case R.id.bottom_messages: {
                    replaceFragment(new MessagesFragment());
                    break;
                }
                case R.id.bottom_profile: {
                    replaceFragment(new ProfileFragment());
                    break;
                }
            }
            return true;
        });

    }


    /// calling function for replace fragment

    private void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
    }


    /// Inflate the menu (top toolbar 3 dot menu)

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_top, menu);

        return true;
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