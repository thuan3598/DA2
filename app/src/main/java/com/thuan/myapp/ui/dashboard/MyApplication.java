package com.thuan.myapp.ui.dashboard;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /* Bật chế độ persistence của Firebase Realtime Database */
        // Điều này cho phép Firebase lưu dữ liệu cục bộ và đồng bộ khi có mạng.
        // Chỉ cần gọi một lần duy nhất.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
