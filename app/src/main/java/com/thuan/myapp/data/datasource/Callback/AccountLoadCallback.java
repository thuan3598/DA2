package com.thuan.myapp.data.datasource.Callback;

import com.thuan.myapp.data.model.Account;

import java.util.List;

public interface AccountLoadCallback {
    void onAccountsLoaded(List<Account> accounts);
    void onError(String errorMessage);
}
