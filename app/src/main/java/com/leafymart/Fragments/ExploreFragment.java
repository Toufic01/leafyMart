package com.leafymart.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Structure.MySingleton;
import com.leafymart.Dialogbox.ProductDetailsDialog;
import com.leafymart.Dialogbox.SearchSuggestionsDialog;
import com.leafymart.Adapter.ImageSliderAdapter;
import com.leafymart.Adapter.PlantAdapter;
import com.leafymart.Model.PlantModel;
import com.leafymart.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExploreFragment extends Fragment {

    private PlantAdapter plantAdapter;
    private final List<PlantModel> plants = new ArrayList<>();
    private ViewPager2 viewPager2;

    CardView indoor, outdoor, seeds, tools;

    EditText searchbar;

    // Debounce handler for text watcher
    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    private static final long SEARCH_DELAY_MS = 600; // debounce delay

    public ExploreFragment() {}

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle b) {
        return inf.inflate(R.layout.fragment_explore, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // ── Toolbar ───────────────────────────────
        Toolbar tb = v.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(tb);
        setHasOptionsMenu(true);

        // ── RecyclerView for Trending plants ──────
        RecyclerView rv = v.findViewById(R.id.trending_plants_recyclerView);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        plantAdapter = new PlantAdapter(getContext(), plants, PlantAdapter.VIEW_TYPE_TRENDING);
        rv.setAdapter(plantAdapter);

        // ── Image slider (unchanged) ──────────────
        viewPager2 = v.findViewById(R.id.imageSlider);
        List<Integer> imgs = Arrays.asList(
                R.drawable.image_plant1, R.drawable.image_plant2,
                R.drawable.image_plant3, R.drawable.image_plant4);
        viewPager2.setAdapter(new ImageSliderAdapter(requireContext(), imgs));
        autoSlideImages();

        // ── Click “View All” (already yours) ──────
        v.findViewById(R.id.trending_plants_viewAll_TV).setOnClickListener(btn -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left,  R.anim.slide_out_right)
                    .replace(R.id.frame_layout, new View_Tranding())
                    .addToBackStack(null)
                    .commit();
        });


        // ── Finally: fetch real data ──────────────

        fetchTrending();

        final Handler handler = new Handler();
        final int interval = 5000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    fetchTrending();
                    handler.postDelayed(this, interval);

            }
        }, interval);


        ///  for categories work

        indoor = v.findViewById(R.id.cardview_indoor);
        outdoor = v.findViewById(R.id.cardview_outdoor);
        seeds = v.findViewById(R.id.cardview_seeds);
        tools = v.findViewById(R.id.cardview_tools);


        ///  Search products
        searchbar = v.findViewById(R.id.search_ET);



        searchbar.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchbar.getText().toString().trim();
                if (!query.isEmpty()) {
                    try {
                        performSearch(query);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter search query", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });


        searchbar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(final Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        try {
                            performSearch(query);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }
        });



        indoor.setOnClickListener(v1 -> {
            navigateToCategory("Indoor");
        });

        outdoor.setOnClickListener(v2 -> {
            navigateToCategory("Outdoor");
        });

        seeds.setOnClickListener(v3 -> {
            navigateToCategory("Seeds");
        });

        tools.setOnClickListener(v4 -> {
            navigateToCategory("Tools");
        });

    }





    private void performSearch(String query) throws UnsupportedEncodingException {
        String url = ApiConfig.BASE_URL + "/products?search=" + URLEncoder.encode(query, "UTF-8");

        JSONObject requestBody = new JSONObject();
        try {
            // Use "search" key if API supports; else "category"
            requestBody.put("search", query);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error preparing search request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest searchRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<PlantModel> searchResults = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            searchResults.add(new PlantModel(
                                    obj.optInt("id"),
                                    obj.optString("name"),
                                    obj.optDouble("price"),
                                    obj.optDouble("rating"),
                                    obj.optInt("sold"),
                                    obj.optInt("sold"), obj.optString("image_url")
                            ));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (searchResults.isEmpty()) {
                        Toast.makeText(getContext(), "No products found for: " + query, Toast.LENGTH_SHORT).show();
                    } else {
                        showSearchSuggestionsDialog(searchResults);
                    }
                },
                error -> Toast.makeText(getContext(), "Search error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
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

        MySingleton.get(requireContext()).add(searchRequest);
    }


    private void showSearchSuggestionsDialog(List<PlantModel> searchResults) {
        SearchSuggestionsDialog dialog = new SearchSuggestionsDialog(requireContext(), searchResults, selectedProduct -> {
            // When user selects a suggestion, show full product details dialog
            showProductDetailsDialog(selectedProduct);
        });
        dialog.show();
    }

    private void showProductDetailsDialog(PlantModel product) {
        ProductDetailsDialog detailsDialog = new ProductDetailsDialog((FragmentActivity) requireContext(), product);
        detailsDialog.show();
    }



    private void navigateToCategory(String categoryName) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // enter from right
                        R.anim.slide_out_left,  // exit to left
                        R.anim.slide_in_left,   // enter from left when popBackStack (back)
                        R.anim.slide_out_right  // exit to right when popBackStack (back)
                )
                .replace(R.id.frame_layout, categories_viewAll.newInstance(categoryName))
                .addToBackStack(null)
                .commit();
    }



    /** Call GET /products/trending and fill RecyclerView */
    private void fetchTrending() {

        if (!isAdded() || getContext() == null || isDetached()) {
            return; // Skip if fragment isn't attached
        }

        String url = ApiConfig.BASE_URL + "/products/trending";

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (!isAdded()) return;
                    plants.clear();
                    parseJsonArray(response);
                    plantAdapter.notifyDataSetChanged();  // refresh RecyclerView
                },
                error -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Load error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        MySingleton.get(requireContext()).add(req);
    }



    private void parseJsonArray(JSONArray arr) {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;

            String imageUrl = o.optString("image_url");
            if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.equalsIgnoreCase("null")) {
                imageUrl = null; // Or fallback image URL
            }

            plants.add(new PlantModel(
                    o.optInt("id"),
                    o.optString("name"),
                    o.optDouble("price"),
                    o.optDouble("rating"),
                    o.optInt("sold"),
                    o.optInt("sold"), imageUrl
            ));
        }
    }




    /** Simple auto‑slide for ViewPager2 */
    private void autoSlideImages() {
        Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override public void run() {
                if (isAdded() && viewPager2.getAdapter() != null) {
                    int next = (viewPager2.getCurrentItem() + 1) % viewPager2.getAdapter().getItemCount();
                    viewPager2.setCurrentItem(next, true);
                    h.postDelayed(this, 2500);
                }
            }
        };
        h.postDelayed(r, 2500);
    }




    // ── Toolbar menu inflating (kept from your code) ─────
    @Override public void onCreateOptionsMenu(Menu m, MenuInflater i) {
        i.inflate(R.menu.menu_top, m);
        super.onCreateOptionsMenu(m, i);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("About LeafyMart")
                .setMessage("LeafyMart v1.0\n\nDeveloped by Md. Toufic Ahmed.\nAn eCommerce app for plant lovers.")
                .setPositiveButton("OK", null)
                .show();
    }


    private void logoutUser() {
        int userId = com.leafymart.Manager.SessionManager.getInstance(requireContext()).getUserId();
        if (userId == -1) {
            Toast.makeText(getContext(), "Already logged out", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = com.leafymart.Structure.ApiConfig.BASE_URL + "/logout?user_id=" + userId;

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET,
                url,
                response -> {
                    // Clear session
                    com.leafymart.Manager.SessionManager.getInstance(requireContext()).clear();
                    Toast.makeText(getContext(), "Logged out & cart cleared", Toast.LENGTH_SHORT).show();

                    // Redirect to LoginActivity or splash
                    Intent intent = new Intent(requireActivity(), com.leafymart.Activities.Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                },
                error -> Toast.makeText(getContext(), "Logout failed", Toast.LENGTH_SHORT).show()
        );

        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request);
    }


}
