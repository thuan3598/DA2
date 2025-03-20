package com.thuan.myapp;

import android.os.Bundle;
import android.widget.TabHost;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetailActivity extends AppCompatActivity {
    TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tabHost = findViewById(R.id.thDetail);
        tabHost.setup();

        // Tạo Tab 1
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Tab 1");
        spec1.setIndicator("", getResources().getDrawable(R.drawable.dam));  // Tiêu đề Tab
        spec1.setContent(R.id.tab1);  // Gán nội dung cho Tab 1
        tabHost.addTab(spec1);

        // Tạo Tab 2
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("",getResources().getDrawable(R.drawable.tide));
        spec2.setContent(R.id.tab2);
        tabHost.addTab(spec2);

        // Chọn Tab đầu tiên mặc định
        tabHost.setCurrentTab(0);
    }
}