package com.thuan.myapp.data.datasource.Callback;

public interface AccountOperationCallback {
    void onSuccess();
    void onError(String errorMessage);
}
