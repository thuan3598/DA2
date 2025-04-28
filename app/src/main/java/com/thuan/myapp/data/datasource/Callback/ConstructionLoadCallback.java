package com.thuan.myapp.data.datasource.Callback;

import com.thuan.myapp.data.model.Account;
import com.thuan.myapp.data.model.Construction;

import java.util.List;

public interface ConstructionLoadCallback {
    void onConstructionsLoaded(List<Construction> constructions);
    void onError(String errorMessage);
}
