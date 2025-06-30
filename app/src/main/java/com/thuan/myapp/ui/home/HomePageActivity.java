package com.thuan.myapp.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson; // Import Gson
import com.thuan.myapp.data.model.Account; // Import lớp Account
import com.thuan.myapp.data.model.Construction;
import com.thuan.myapp.R;
import com.thuan.myapp.ui.adapter.ConstructionAdapter;
import com.thuan.myapp.ui.dashboard.AccountActivity;
import com.thuan.myapp.ui.dashboard.BaseActivity;
import com.thuan.myapp.ui.dashboard.DetailActivity;
import com.thuan.myapp.ui.dashboard.ExportActivity;
import com.thuan.myapp.ui.dashboard.MapActivity;
import com.thuan.myapp.ui.dashboard.StatisticActivity;
import android.content.SharedPreferences; // Import SharedPreferences

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePageActivity extends BaseActivity {

    CardView cvNew, cvAccount, cvMap, cvStatistic, cvExportData;
    Map<String, Boolean> existingDailyWaterLevelIds;
    DatabaseReference constructionsRef;
    DatabaseReference dailyWaterLevelIdsRef;
    List<Construction> constructionList;

    // Thay đổi: không cần usersRef và currentUserRole nữa, chúng ta sẽ dùng Account từ SharedPreferences
    // DatabaseReference usersRef; // Không cần nữa
    // private String currentUserRole = null; // Không cần nữa

    private Account loggedInAccount; // Biến để lưu trữ đối tượng Account của người dùng hiện tại
    private SharedPreferences sharedPreferences; // SharedPreferences instance
    private Gson gson; // Gson instance

    // Phương thức kiểm tra kết nối mạng (đã có)
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(R.string.home);
        }

        constructionsRef = FirebaseDatabase.getInstance().getReference("constructions");
        dailyWaterLevelIdsRef = FirebaseDatabase.getInstance().getReference("dailyWaterLevelIds");

        // Khởi tạo SharedPreferences và Gson
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        gson = new Gson();
        loadLoggedInAccount(); // Tải thông tin tài khoản đã đăng nhập

        // Bật đồng bộ hóa offline cho các tham chiếu cần thiết
        constructionsRef.keepSynced(true);
        dailyWaterLevelIdsRef.keepSynced(true);

        // Load dữ liệu
        loadConstructionFromFirebase();
        loadDailyWaterLevelIdsFromFirebase();
        // Không cần gọi loadCurrentUserRole() nữa

        cvNew = findViewById(R.id.cvNew);
        cvAccount = findViewById(R.id.cvAccount);
        cvMap = findViewById(R.id.cvMap);
        cvStatistic = findViewById(R.id.cvStatistic);
        cvExportData = findViewById(R.id.cvExportData);

        // --- cvNew (Không yêu cầu mạng) ---
        cvNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đảm bảo constructionList đã được tải trước khi mở dialog
                if (constructionList == null || constructionList.isEmpty()) {
                    Toast.makeText(HomePageActivity.this, "Đang tải danh sách công trình, vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_logbook_choices, null);
                builder.setView(dialogView);

                AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteLogbook);
                EditText edtDatePicker = dialogView.findViewById(R.id.edtDatePicker);
                Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

                ConstructionAdapter adapter = new ConstructionAdapter(HomePageActivity.this, constructionList);
                autoCompleteTextView.setAdapter(adapter);
                autoCompleteTextView.setThreshold(1);

                AlertDialog dialog = builder.create();
                dialog.show();

                edtDatePicker.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            HomePageActivity.this,
                            (view1, selectedYear, selectedMonth, selectedDay) -> {
                                String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                edtDatePicker.setText(selectedDate);
                            },
                            year, month, day);
                    datePickerDialog.show();
                });

                btnSubmit.setOnClickListener(v -> {
                    String selectedName = autoCompleteTextView.getText().toString().trim();
                    String selectedDate = edtDatePicker.getText().toString().trim();

                    if (selectedName.isEmpty()) {
                        autoCompleteTextView.setError("Vui lòng chọn một công trình");
                        autoCompleteTextView.requestFocus();
                        return;
                    }

                    Construction selectedConstruction = null;
                    for (Construction c : constructionList) {
                        if (c.getConstructionName().equals(selectedName)) {
                            selectedConstruction = c;
                            break;
                        }
                    }

                    if (selectedConstruction == null) {
                        autoCompleteTextView.setError("Công trình không hợp lệ");
                        autoCompleteTextView.requestFocus();
                        return;
                    }

                    if (selectedDate.isEmpty()) {
                        edtDatePicker.setError("Vui lòng chọn ngày");
                        edtDatePicker.requestFocus();
                        return;
                    }

                    String combinedId = (selectedConstruction.getId() + "_" + selectedDate).replace("/", "-");

                    boolean recordExists = existingDailyWaterLevelIds != null && existingDailyWaterLevelIds.containsKey(combinedId);
                    boolean isOnline = isNetworkConnected();

                    if (recordExists) {
                        if (!isOnline) {
                            Toast.makeText(HomePageActivity.this, "Không có kết nối mạng. Không thể chỉnh sửa bản ghi đã tồn tại.", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            Toast.makeText(HomePageActivity.this, "Bản ghi đã tồn tại. Chuyển sang chế độ chỉnh sửa.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(HomePageActivity.this, "Bản ghi chưa tồn tại. Chuyển sang chế độ tạo mới.", Toast.LENGTH_SHORT).show();
                    }

                    Intent intent = new Intent(HomePageActivity.this, DetailActivity.class);
                    intent.putExtra("construction", selectedConstruction);
                    intent.putExtra("date", selectedDate);
                    startActivity(intent);
                    dialog.dismiss();
                });
            }
        });

        // --- cvMap (Yêu cầu mạng) ---
        cvMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkConnected()) {
                    Toast.makeText(HomePageActivity.this, "Cần có kết nối mạng để truy cập tính năng bản đồ.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(HomePageActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        // --- cvAccount (Yêu cầu mạng VÀ role admin) ---
        cvAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkConnected()) {
                    Toast.makeText(HomePageActivity.this, "Cần có kết nối mạng để truy cập cài đặt tài khoản.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra role của người dùng từ đối tượng Account đã tải
                if (loggedInAccount == null || loggedInAccount.getRole() == null) {
                    Toast.makeText(HomePageActivity.this, "Không thể xác định quyền tài khoản, vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d("HomePageActivity", "Role of logged in user: " + loggedInAccount.getRole());

                if (!"admin".equalsIgnoreCase(loggedInAccount.getRole())) {
                    Toast.makeText(HomePageActivity.this, "Bạn không có quyền truy cập tính năng này (chỉ dành cho Admin).", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(HomePageActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        // --- cvStatistic (Yêu cầu mạng) ---
        cvStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkConnected()) {
                    Toast.makeText(HomePageActivity.this, "Cần có kết nối mạng để truy cập tính năng thống kê.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(HomePageActivity.this, StatisticActivity.class);
                startActivity(intent);
            }
        });

        // --- cvExportData (Yêu cầu mạng) ---
        cvExportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkConnected()) {
                    Toast.makeText(HomePageActivity.this, "Cần có kết nối mạng để truy cập tính năng xuất dữ liệu.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(HomePageActivity.this, ExportActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadConstructionFromFirebase() {
        constructionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                constructionList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Construction c = child.getValue(Construction.class);
                    if (c != null) {
                        constructionList.add(c);
                    }
                }
                Log.d("HomePageActivity", "Loaded " + (constructionList != null ? constructionList.size() : 0) + " constructions.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomePageActivity", "Failed to load constructions: " + error.getMessage());
                Toast.makeText(HomePageActivity.this, "Không thể tải danh sách công trình: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDailyWaterLevelIdsFromFirebase() {
        existingDailyWaterLevelIds = new HashMap<>();
        dailyWaterLevelIdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                existingDailyWaterLevelIds.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    existingDailyWaterLevelIds.put(child.getKey(), true);
                }
                Log.d("HomePageActivity", "Loaded " + existingDailyWaterLevelIds.size() + " DailyWaterLevel IDs.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomePageActivity", "Failed to load DailyWaterLevel IDs: " + error.getMessage());
                Toast.makeText(HomePageActivity.this, "Không thể tải danh sách ID mực nước: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức để tải đối tượng Account đã đăng nhập từ SharedPreferences
    private void loadLoggedInAccount() {
        String jsonAccount = sharedPreferences.getString("loggedInAccountJson", null);
        if (jsonAccount != null) {
            loggedInAccount = gson.fromJson(jsonAccount, Account.class);
            if (loggedInAccount != null) {
                Log.d("HomePageActivity", "Loaded Account from SharedPreferences: " + loggedInAccount.getEmail() + ", Role: " + loggedInAccount.getRole());
            } else {
                Log.e("HomePageActivity", "Failed to parse Account from JSON in SharedPreferences.");
            }
        } else {
            Log.d("HomePageActivity", "No logged in account found in SharedPreferences.");
            // Xử lý trường hợp không có account (ví dụ: đăng xuất hoặc lỗi)
            // Bạn có thể chuyển hướng người dùng về màn hình đăng nhập nếu không có thông tin account
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}