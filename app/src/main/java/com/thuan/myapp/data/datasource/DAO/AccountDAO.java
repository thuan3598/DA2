package com.thuan.myapp.data.datasource.DAO;

import com.thuan.myapp.data.datasource.Callback.AccountLoadCallback;
import com.thuan.myapp.data.datasource.Callback.AccountOperationCallback;
import com.thuan.myapp.data.model.Account;

import java.util.List;

public interface AccountDAO {

    void loadListAccount(AccountLoadCallback callback);
    void createAccount(Account account, AccountOperationCallback callback);
    void updateAccount(String id, Account account, AccountOperationCallback callback);
    void deleteAccount(String id, AccountOperationCallback callback);


}
