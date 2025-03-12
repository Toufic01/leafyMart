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

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
// for using same theme for light and dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setStatusBarColor(getResources().getColor(R.color.light_grey));

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
                    return insets;
                });

        // Initialize the first fragment
        if (savedInstanceState == null) {
            replaceFragment(new ExploreFragment(), false, true);
            currentFragment = new ExploreFragment();
        }


        // Bottom navigation setup
        binding.bottomNavView.setBackground(null);
        binding.bottomNavView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.bottom_explore:
                    selectedFragment = new ExploreFragment();
                    break;
                case R.id.bottom_favorites:
                    selectedFragment = new FavoritesFragment();
                    break;
                case R.id.bottom_cart:
                    selectedFragment = new CartFragment();
                    break;
                case R.id.bottom_messages:
                    selectedFragment = new MessagesFragment();
                    break;
                case R.id.bottom_profile:
                    selectedFragment = new ProfileFragment();
                    break;
            }
            if (selectedFragment != null && !selectedFragment.getClass().equals(currentFragment.getClass())) {
                // Determine navigation direction
                boolean isForward = isForwardNavigation(selectedFragment);
                replaceFragment(selectedFragment, true, isForward);
                currentFragment = selectedFragment;
            }
            return true;
        });
    }

    // Replace fragment method with animations
    private void replaceFragment(Fragment fragment, boolean addToBackStack, boolean isForward) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Set animations based on direction
        if (isForward) {
            // Forward navigation: slide in from right, slide out to left
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,  // Enter animation
                    R.anim.slide_out_left   // Exit animation
            );
        } else {
            // Backward navigation: slide in from left, slide out to right
            transaction.setCustomAnimations(
                    R.anim.slide_in_left,   // Enter animation
                    R.anim.slide_out_right  // Exit animation
            );
        }

        transaction.replace(R.id.frame_layout, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName()); // Add to back stack
        }

        transaction.commit();
    }

    /// for clear transaction with proper animation
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Pop the back stack to trigger the backward animations
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed(); // Exit the app if no fragments are in the back stack
        }
    }



    /// for button nav animation determine which is Forward Navigation
    private boolean isForwardNavigation(Fragment newFragment) {
        // Define the order of fragments
        List<Class<? extends Fragment>> fragmentOrder = Arrays.asList(
                ExploreFragment.class,
                FavoritesFragment.class,
                CartFragment.class,
                MessagesFragment.class,
                ProfileFragment.class
        );

        // Get the indices of the current and new fragments
        int currentIndex = fragmentOrder.indexOf(currentFragment.getClass());
        int newIndex = fragmentOrder.indexOf(newFragment.getClass());

        // If the new fragment is later in the order, it's a forward navigation
        return newIndex > currentIndex;
    }


}