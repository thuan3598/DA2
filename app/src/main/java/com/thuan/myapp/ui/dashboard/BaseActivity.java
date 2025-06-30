package com.thuan.myapp.ui.dashboard;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.thuan.myapp.R; // Đảm bảo đúng package R của bạn
import com.thuan.myapp.data.model.Account;
import com.thuan.myapp.ui.dashboard.AccountActivity;
import com.thuan.myapp.ui.home.HomePageActivity;
import com.thuan.myapp.ui.login.MainActivity;

import java.util.Locale;

// Đây là lớp BaseActivity mà các Activity khác có NavigationView sẽ kế thừa
public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout; // Protected để các lớp con có thể truy cập
    protected NavigationView navigationView; // Protected để các lớp con có thể truy cập
    protected SharedPreferences sharedPreferences;
    protected Gson gson;
    protected FirebaseAuth firebaseAuth;

    protected FrameLayout contentFrame; // Khung để chứa nội dung của Activity con
    protected ImageView imgMenuIcon;
    protected TextView tvHeaderTitle;

    // Khóa cho SharedPreferences
    private static final String PREF_NAME = "LoginPrefs";
    protected static final String KEY_ACCOUNT_JSON = "loggedInAccountJson";
    protected static final String KEY_THEME_MODE = "currentThemeMode";
    protected static final String KEY_LANGUAGE = "currentLanguage";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);
        // Khởi tạo các đối tượng chung
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        firebaseAuth = FirebaseAuth.getInstance();

        // Ánh xạ các thành phần từ activity_base.xml
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        contentFrame = findViewById(R.id.content_frame);
        imgMenuIcon = findViewById(R.id.img_menu_icon);
        tvHeaderTitle = findViewById(R.id.tv_header_title);

        // Thiết lập sự kiện click cho icon menu
        if (imgMenuIcon != null) {
            imgMenuIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        }
        applySavedLanguage(this);
        applySavedTheme();

        setupNavigationView(); // Thiết lập listener cho NavigationView
    }

    // Phương thức này được Activity con gọi để thêm layout của nó vào contentFrame
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Nếu layoutResID là layout cơ sở, thì gọi super.setContentView
        if (layoutResID == R.layout.activity_base) {
            super.setContentView(layoutResID);
        } else if (contentFrame != null) {
            // Nếu không, inflate layout của Activity con vào contentFrame
            getLayoutInflater().inflate(layoutResID, contentFrame, true);
        } else {
            // Trường hợp contentFrame chưa được khởi tạo (có thể xảy ra nếu setContentView(R.layout.activity_base) chưa chạy)
            super.setContentView(layoutResID); // fallback
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = newBase.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, "vi");
        Locale locale = new Locale(lang);
        Configuration configuration = newBase.getResources().getConfiguration();
        configuration.setLocale(locale);
        super.attachBaseContext(newBase.createConfigurationContext(configuration));
    }

    // Phương thức này sẽ được gọi bởi các Activity con để thiết lập NavigationView
    protected void setupNavigationView() {
        drawerLayout = findViewById(R.id.drawer_layout); // ID của DrawerLayout trong activity_home_page.xml
        navigationView = findViewById(R.id.nav_view); // ID của NavigationView trong activity_home_page.xml

        if (navigationView == null || drawerLayout == null) {
            // Log lỗi hoặc xử lý nếu Activity không có NavigationView/DrawerLayout
            return;
        }

        // Lấy View của header từ NavigationView
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

        // Lấy thông tin tài khoản từ SharedPreferences
        String jsonAccount = sharedPreferences.getString(KEY_ACCOUNT_JSON, null);
        if (jsonAccount != null) {
            Account loggedInAccount = gson.fromJson(jsonAccount, Account.class);
            if (loggedInAccount != null) {
                navHeaderName.setText(loggedInAccount.getName());
                navHeaderEmail.setText(loggedInAccount.getEmail());
            } else {
                navHeaderName.setText("Guest");
                navHeaderEmail.setText("guest@example.com");
            }
        } else {
            navHeaderName.setText("Guest");
            navHeaderEmail.setText("guest@example.com");
        }

        // Thiết lập listener cho các item trong Navigation Drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Chuyển về Home (nếu chưa phải HomeActivity)
                    if (!getClass().getName().equals(HomePageActivity.class.getName())) {
                        startActivity(new Intent(BaseActivity.this, HomePageActivity.class));
                    }
                } else if (id == R.id.nav_settings) {
                    // Chuyển đến màn hình thông tin người dùng
                    if (!getClass().getName().equals(ProfileActivity.class.getName())) {
                        startActivity(new Intent(BaseActivity.this, ProfileActivity.class));
                    }
                } else if (id == R.id.nav_info) {
                    Toast.makeText(BaseActivity.this, "Help clicked", Toast.LENGTH_SHORT).show();
                    // Chuyển đến màn hình trợ giúp/thông tin
                } else if (id == R.id.nav_change_theme) { // Xử lý sự kiện cho nút Thay đổi Theme
                    toggleAppTheme();
                } else if (id == R.id.nav_change_language) { // Xử lý sự kiện cho nút Thay đổi Ngôn ngữ
                    toggleAppLanguage();
                } else if (id == R.id.nav_logout) {
                    logoutUser();
                }

                drawerLayout.closeDrawer(GravityCompat.START); // Đóng drawer sau khi chọn item
                return true;
            }
        });
    }

    // Phương thức đăng xuất chung
    protected void logoutUser() {
        firebaseAuth.signOut();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
    }


    private void toggleAppTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        int newThemeMode;

        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            // Đang ở chế độ tối, chuyển sang chế độ sáng
            newThemeMode = AppCompatDelegate.MODE_NIGHT_NO;
            Toast.makeText(this, "Chuyển sang chế độ sáng", Toast.LENGTH_SHORT).show();
        } else {
            // Đang ở chế độ sáng, chuyển sang chế độ tối
            newThemeMode = AppCompatDelegate.MODE_NIGHT_YES;
            Toast.makeText(this, "Chuyển sang chế độ tối", Toast.LENGTH_SHORT).show();
        }

        // Lưu trạng thái theme mới vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME_MODE, newThemeMode);
        editor.apply();

        // Áp dụng theme mới
        AppCompatDelegate.setDefaultNightMode(newThemeMode);

        // Recreate Activity để theme được áp dụng ngay lập tức
        recreate();
    }

    // Phương thức để áp dụng theme đã lưu khi khởi động Activity
    private void applySavedTheme() {
        int savedThemeMode = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); // Mặc định theo hệ thống
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);
    }

    private void toggleAppLanguage() {
        String currentLang = sharedPreferences.getString(KEY_LANGUAGE, "vi"); // Mặc định là 'vi'
        String newLang;

        if (currentLang.equals("vi")) {
            newLang = "en";
            Toast.makeText(this, "Switched to English", Toast.LENGTH_SHORT).show();
        } else {
            newLang = "vi";
            Toast.makeText(this, "Chuyển sang Tiếng Việt", Toast.LENGTH_SHORT).show();
        }

        // Lưu ngôn ngữ mới vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LANGUAGE, newLang);
        editor.apply();

        // Áp dụng ngôn ngữ mới và khởi động lại toàn bộ ứng dụng
        applySavedLanguage(this);
        // Khởi động lại toàn bộ ứng dụng để các chuỗi được cập nhật đúng cách
        Intent refresh = new Intent(this, MainActivity.class); // Hoặc HomePageActivity nếu MainActivity là Login
        refresh.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(refresh);
        finishAffinity(); // Đóng tất cả các activity cũ
    }


    public void applySavedLanguage(Context context) {
        String lang = sharedPreferences.getString(KEY_LANGUAGE, "vi"); // Mặc định là 'vi'
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            context.createConfigurationContext(config);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }


    // Override onBackPressed để đóng Drawer nếu đang mở
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}