package com.thuan.myapp.data.datasource.Impl;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thuan.myapp.data.datasource.Callback.ConstructionLoadCallback;
import com.thuan.myapp.data.datasource.Callback.DailyWaterLevelLoadCallback;
import com.thuan.myapp.data.datasource.DAO.DailyWaterLevelDAO;
import com.thuan.myapp.data.model.Construction;
import com.thuan.myapp.data.model.DailyWaterLevel;

import java.util.ArrayList;
import java.util.List;

public class DailyWaterLevelDAOImpl implements DailyWaterLevelDAO {
    private DatabaseReference dailyWaterLevelsRef;

    public DailyWaterLevelDAOImpl() {
        dailyWaterLevelsRef = FirebaseDatabase.getInstance().getReference("dailywaterLevel");
    }
    @Override
    public void loadListDailyWaterLevel(DailyWaterLevelLoadCallback callback) {
        List<DailyWaterLevel> list = new ArrayList<>();

        dailyWaterLevelsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    DailyWaterLevel dailyWaterLevel = child.getValue(DailyWaterLevel.class);
                    if (dailyWaterLevel != null) {
                        dailyWaterLevel.setId(child.getKey()); // Gán key của node làm ID
                        list.add(dailyWaterLevel);
                        Log.d("DailyWaterLevelDAO", dailyWaterLevel.toString());
                    }
                }
                callback.onDailyWaterLevelsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Failed to load constructions: " + error.getMessage());
            }
        });
    }
}
