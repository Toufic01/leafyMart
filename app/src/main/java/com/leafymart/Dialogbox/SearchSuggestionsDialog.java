package com.leafymart.Dialogbox;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leafymart.Model.PlantModel;

import java.util.List;

// A dialog showing search suggestions in a small list
public class SearchSuggestionsDialog extends androidx.appcompat.app.AlertDialog {

    public interface OnProductSelectedListener {
        void onProductSelected(PlantModel product);
    }

    private final List<PlantModel> suggestions;
    private final OnProductSelectedListener listener;

    public SearchSuggestionsDialog(@NonNull Context context,
                                   List<PlantModel> suggestions,
                                   OnProductSelectedListener listener) {
        super(context);
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create RecyclerView programmatically or inflate from XML (better)
        RecyclerView rv = new RecyclerView(getContext());
        rv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(200)  // limit height to ~200dp
        ));
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        SuggestionAdapter adapter = new SuggestionAdapter(suggestions, product -> {
            dismiss(); // dismiss suggestions dialog
            listener.onProductSelected(product); // callback
        });

        rv.setAdapter(adapter);
        setContentView(rv);
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    // Adapter for suggestion list
    static class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
        private final List<PlantModel> data;
        private final OnClickListener listener;

        interface OnClickListener {
            void onClick(PlantModel product);
        }

        SuggestionAdapter(List<PlantModel> data, OnClickListener listener) {
            this.data = data;
            this.listener = listener;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTv;
            ViewHolder(View itemView) {
                super(itemView);
                nameTv = itemView.findViewById(android.R.id.text1);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PlantModel product = data.get(position);
            holder.nameTv.setText(product.getName());
            holder.itemView.setOnClickListener(v -> listener.onClick(product));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
