package com.thuan.myapp.data.datasource.DAO;

import com.thuan.myapp.data.datasource.Callback.AccountLoadCallback;
import com.thuan.myapp.data.datasource.Callback.DailyWaterLevelLoadCallback;

public interface DailyWaterLevelDAO {
    void loadListDailyWaterLevel(DailyWaterLevelLoadCallback callback);
}
