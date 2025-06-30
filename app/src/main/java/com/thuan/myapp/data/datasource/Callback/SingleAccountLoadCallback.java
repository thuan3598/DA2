package com.thuan.myapp.data.datasource.Callback;

import com.thuan.myapp.data.model.Account;

public interface SingleAccountLoadCallback {
    void onAccountLoaded(Account account);
    void onError(String errorMessage);
}