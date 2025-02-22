package com.leafymart.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

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
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_green));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

}