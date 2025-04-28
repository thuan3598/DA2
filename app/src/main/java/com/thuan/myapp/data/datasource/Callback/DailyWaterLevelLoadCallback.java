package com.thuan.myapp.data.datasource.Callback;

import com.thuan.myapp.data.model.Account;
import com.thuan.myapp.data.model.DailyWaterLevel;

import java.util.List;

public interface DailyWaterLevelLoadCallback {
    void onDailyWaterLevelsLoaded(List<DailyWaterLevel> DailyWaterLevels);
    void onError(String errorMessage);
}
