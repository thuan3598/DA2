package com.thuan.myapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.thuan.myapp.R;
import com.thuan.myapp.data.model.Construction;

import java.util.List;

public class ConstructionAdapter1 extends ArrayAdapter<Construction> {

    private List<Construction> constructions;

    public ConstructionAdapter1(Context context, int resource, List<Construction> objects) {
        super(context, resource, objects);
        this.constructions = objects;
    }

    @Override
    public int getCount() {
        return constructions.size();
    }

    @Override
    public Construction getItem(int position) {
        return constructions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
        android.view.LayoutInflater inflater = (android.view.LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        android.view.View view = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        android.widget.TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(constructions.get(position).getConstructionName()); // Hiển thị tên công trình
        return view;
    }
}
