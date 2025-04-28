package com.thuan.myapp.data.datasource.DAO;

import com.thuan.myapp.data.datasource.Callback.AccountLoadCallback;
import com.thuan.myapp.data.datasource.Callback.ConstructionLoadCallback;

public interface ConstructionDAO {
    void loadListConstruction(ConstructionLoadCallback callback);
}
