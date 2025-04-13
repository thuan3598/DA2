package com.thuan.myapp.ui.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thuan.myapp.data.model.Construction;

import java.util.ArrayList;
import java.util.List;

public class ConstructionAdapter extends ArrayAdapter<Construction> {

    private List<Construction> originalList;
    private List<Construction> filteredList;
    private Context context;

    public ConstructionAdapter(Context context, List<Construction> constructionList) {
        super(context, android.R.layout.simple_dropdown_item_1line, constructionList);
        this.originalList = new ArrayList<>(constructionList);
        this.filteredList = constructionList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Nullable
    @Override
    public Construction getItem(int position) {
        return filteredList.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Construction> suggestions = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    suggestions.addAll(originalList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Construction item : originalList) {
                        if (item.getConstructionName().toLowerCase().contains(filterPattern)) {
                            suggestions.add(item);
                        }
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<Construction>) results.values;
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((Construction) resultValue).getConstructionName();
            }
        };
    }
}
