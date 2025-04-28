package com.thuan.myapp.data.datasource.Impl;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thuan.myapp.data.datasource.Callback.ConstructionLoadCallback;
import com.thuan.myapp.data.datasource.DAO.ConstructionDAO;
import com.thuan.myapp.data.model.Construction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ConstructionDAOImpl implements ConstructionDAO {
    private DatabaseReference constructionsRef;
    
    public ConstructionDAOImpl() {
        constructionsRef = FirebaseDatabase.getInstance().getReference("constructions");
    }
    @Override
    public void loadListConstruction(ConstructionLoadCallback callback) {
        List<Construction> list = new ArrayList<>();

        constructionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Construction construction = child.getValue(Construction.class);
                    if (construction != null) {
                        construction.setId(child.getKey()); // Gán key của node làm ID
                        list.add(construction);
                        Log.d("ConstructionDAO", construction.toString());
                    }
                }
                callback.onConstructionsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Failed to load constructions: " + error.getMessage());
            }
        });
    }
}
