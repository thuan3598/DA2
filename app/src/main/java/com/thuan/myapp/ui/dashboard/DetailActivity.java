package com.thuan.myapp.ui.dashboard;

import android.content.Context; // Import Context
import android.content.Intent;
import android.net.ConnectivityManager; // Import ConnectivityManager
import android.net.NetworkInfo; // Import NetworkInfo
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.thuan.myapp.data.model.Construction;
import com.thuan.myapp.data.model.DailyWaterLevel;
import com.thuan.myapp.R;

public class DetailActivity extends BaseActivity {
    TabHost tabHost;
    CardView cvWater_level_7h, cvWater_level_19h, cvGate_open_height, cvOpened_gate_count, cvWaterFlow, cvNote;
    EditText edtDate, edtWater_level_7h, edtWater_level_19h, edtGate_open_height, edtOpened_gate_count, edtWaterFlow, edtNote, edtComstructName, edtYear_built, edtLocation,
            edtGateType, edtGateCount, edtGateSize,
            edtDesignedFlow, edtDesignedWaterLevel, edtBottomElevation, edtWaterGaugeType;
    Button btnSave, btnDelete, btnCreate1, btnUpdate1, btnDelete1;
    DailyWaterLevel dwl = null;
    Construction construction;
    String date;

    private EditText currentEditText = null;

    DatabaseReference refDailyWaterLevel;
    DatabaseReference refConstructions;
    DatabaseReference refDailyWaterLevelIds;

    // Phương thức kiểm tra kết nối mạng
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
        setContentView(R.layout.activity_detail);

        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(R.string.add_data);
        }

        tabHost = findViewById(R.id.thDetail);
        tabHost.setup();

        TabHost.TabSpec spec1 = tabHost.newTabSpec("Tab 1");
        spec1.setIndicator("", getResources().getDrawable(R.drawable.dam));
        spec1.setContent(R.id.tab1);
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("", getResources().getDrawable(R.drawable.tide));
        spec2.setContent(R.id.tab2);
        tabHost.addTab(spec2);

        tabHost.setCurrentTab(0);

        construction = (Construction) getIntent().getSerializableExtra("construction");
        date = getIntent().getStringExtra("date");

        refDailyWaterLevel = FirebaseDatabase.getInstance().getReference("dailywaterLevel");
        refConstructions = FirebaseDatabase.getInstance().getReference("constructions");
        refDailyWaterLevelIds = FirebaseDatabase.getInstance().getReference("dailyWaterLevelIds");

        refConstructions.keepSynced(true);
        refDailyWaterLevelIds.keepSynced(true);

        getDailyWaterLevelById(construction.getId(), date, new DailyWaterLevelCallback() {
            @Override
            public void onResult(@Nullable DailyWaterLevel result) {
                if (result != null) {
                    Log.d("Firebase", "Tìm thấy bản ghi: " + result.getWaterFlow());
                    dwl = result;
                    Log.d("dwl1", dwl.getConstructionId());
                    initDailyWaterData();
                } else {
                    Log.d("Firebase", "Không tìm thấy bản ghi");
                    initDailyWaterData();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Firebase", "Lỗi khi truy vấn", e);
                Toast.makeText(DetailActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                initDailyWaterData();
            }
        });

        initConstructionData();
    }

    public void initDailyWaterData() {
        cvWater_level_7h = findViewById(R.id.cvWater_level_7h);
        cvWater_level_19h = findViewById(R.id.cvWater_level_19h);
        cvGate_open_height = findViewById(R.id.cvGate_open_height);
        cvOpened_gate_count = findViewById(R.id.cvOpened_gate_count);
        cvWaterFlow = findViewById(R.id.cvWaterFlow);
        cvNote = findViewById(R.id.cvNote);
        edtDate = findViewById(R.id.edtDate);
        edtWater_level_7h = findViewById(R.id.edtWater_level_7h);
        edtWater_level_19h = findViewById(R.id.edtWater_level_19h);
        edtGate_open_height = findViewById(R.id.edtGate_open_height);
        edtOpened_gate_count = findViewById(R.id.edtOpened_gate_count);
        edtWaterFlow = findViewById(R.id.edtWaterFlow);
        edtNote = findViewById(R.id.edtNote);

        edtDate.setText(date);
        edtDate.setClickable(false);
        edtDate.setFocusable(false);
        edtDate.setEnabled(false);

        if (dwl != null) {
            edtWater_level_7h.setText(dwl.getWaterLevel7h() != null ? dwl.getWaterLevel7h().toString() : "");
            edtWater_level_19h.setText(dwl.getWaterLevel19h() != null ? dwl.getWaterLevel19h().toString() : "");
            edtGate_open_height.setText(dwl.getGateOpenHeight() != null ? dwl.getGateOpenHeight().toString() : "");
            edtOpened_gate_count.setText(dwl.getOpenedGateCount() != null ? dwl.getOpenedGateCount().toString() : "");
            edtWaterFlow.setText(dwl.getWaterFlow() != null ? dwl.getWaterFlow().toString() : "");
            edtNote.setText(dwl.getNotes() != null ? dwl.getNotes() : "");
        }

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        if (dwl == null) {
            btnDelete.setEnabled(false);
            btnDelete.setAlpha(0.5F);
        } else {
            btnDelete.setEnabled(true); // Đảm bảo nút xóa được bật nếu có dữ liệu
            btnDelete.setAlpha(1.0F);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DailyWaterLevel data = new DailyWaterLevel();
                String constructionId = construction.getId();
                String recorderId = "0"; // Cần thay thế bằng ID người dùng thực tế

                data.setConstructionId(constructionId);
                data.setDate(date);
                Double waterLevel7h = parseDouble(edtWater_level_7h);
                Double waterLevel19h = parseDouble(edtWater_level_19h);

                data.setWaterLevel7h(waterLevel7h);
                data.setWaterLevel19h(waterLevel19h);

                // Tính toán AvgWaterLevel nếu không được nhập và cả 7h/19h đều có giá trị
                if (waterLevel7h != null && waterLevel19h != null) {
                    data.setAvgWaterLevel((waterLevel19h + waterLevel7h) / 2.0);
                } else if((waterLevel7h != null)&&(waterLevel19h == null)){
                    data.setAvgWaterLevel(parseDouble(edtWater_level_7h));
                } else if ((waterLevel7h == null)&&(waterLevel19h != null)){
                    data.setAvgWaterLevel(parseDouble(edtWater_level_19h));
                }
                else{
                    data.setAvgWaterLevel(0.0);
                }

                data.setGateOpenHeight(parseDouble(edtGate_open_height));
                data.setOpenedGateCount(parseInt(edtOpened_gate_count));
                data.setWaterFlow(parseDouble(edtWaterFlow));
                data.setNotes(edtNote.getText().toString().trim());
                data.setRecorderId(recorderId);

                // Xử lý ID của bản ghi
                if (dwl == null) { // Tạo mới
                    data.setId((constructionId + "_" + date).replace("/", "-"));
                } else { // Cập nhật bản ghi đã tồn tại
                    data.setId(dwl.getId());
                }

                // Gọi saveData với đối tượng DailyWaterLevel đã được điền đầy đủ
                saveData(data);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteData();
            }
        });
    }

    public void deleteData() {
        if (dwl == null || dwl.getId() == null) {
            Toast.makeText(this, "Không có dữ liệu để xóa.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String deletedId = dwl.getId();

        if (!isNetworkConnected()) {
            Toast.makeText(this, "Không có kết nối mạng. Thao tác xóa sẽ được thực hiện khi có mạng trở lại.", Toast.LENGTH_LONG).show();
            // Firebase sẽ tự động xếp hàng đợi thao tác xóa này
            refDailyWaterLevel.child(deletedId).removeValue();
            refDailyWaterLevelIds.child(deletedId).removeValue();
            clearDailyWaterLevelFields();
            btnDelete.setEnabled(false);
            btnDelete.setAlpha(0.5F);
            dwl = null; // Cập nhật trạng thái cục bộ ngay lập tức
            return;
        }

        // Nếu có mạng, thực hiện xóa và đợi callback
        refDailyWaterLevel.child(deletedId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Xóa ID khỏi node dailyWaterLevelIds
                        refDailyWaterLevelIds.child(deletedId).removeValue();
                        Toast.makeText(this, "Dữ liệu đã được xóa thành công!", Toast.LENGTH_SHORT).show();
                        clearDailyWaterLevelFields();
                        btnDelete.setEnabled(false);
                        btnDelete.setAlpha(0.5F);
                        dwl = null; // Đặt lại dwl thành null sau khi xóa
                    } else {
                        Toast.makeText(this, "Xóa dữ liệu thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void saveData(DailyWaterLevel data) {
        if (!isNetworkConnected()) {
            Toast.makeText(this, "Không có kết nối mạng. Dữ liệu sẽ được lưu offline và đồng bộ khi có mạng trở lại.", Toast.LENGTH_LONG).show();
            // Firebase sẽ tự động xếp hàng đợi thao tác lưu này
            refDailyWaterLevel.child(data.getId()).setValue(data);
            refDailyWaterLevelIds.child(data.getId()).setValue(true); // Ghi một giá trị đơn giản
            btnDelete.setEnabled(true);
            btnDelete.setAlpha(1.0F);
            dwl = data; // Cập nhật dwl ngay lập tức để phản ánh trạng thái cục bộ
            return;
        }

        // Nếu có mạng, thực hiện lưu và đợi callback
        refDailyWaterLevel.child(data.getId()).setValue(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Thêm/Cập nhật ID vào node dailyWaterLevelIds
                        refDailyWaterLevelIds.child(data.getId()).setValue(true); // Ghi một giá trị đơn giản
                        Toast.makeText(this, "Dữ liệu đã được lưu thành công!", Toast.LENGTH_SHORT).show();
                        btnDelete.setEnabled(true);
                        btnDelete.setAlpha(1.0F);
                        dwl = data; // Cập nhật dwl sau khi lưu thành công
                    } else {
                        Toast.makeText(this, "Lưu dữ liệu thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Các phương thức initConstructionData, matchData, saveConstructionData, deleteConstructionData
    // Vẫn giữ nguyên, chúng sẽ hoạt động offline nhờ setPersistenceEnabled(true)
    // Và các thao tác lưu/xóa/cập nhật trên refConstructions sẽ được hàng đợi

    public void initConstructionData() {
        edtComstructName = findViewById(R.id.edtComstructName);
        edtYear_built = findViewById(R.id.edtYear_built);
        edtLocation = findViewById(R.id.edtLocation);
        edtGateType = findViewById(R.id.edtGateType);
        edtGateCount = findViewById(R.id.edtGateCount);
        edtGateSize = findViewById(R.id.edtGateSize);
        edtDesignedFlow = findViewById(R.id.edtDesignedFlow);
        edtDesignedWaterLevel = findViewById(R.id.edtDesignedWaterLevel);
        edtBottomElevation = findViewById(R.id.edtBottomElevation);
        edtWaterGaugeType = findViewById(R.id.edtWaterGaugeType);

        edtComstructName.setText(construction.getConstructionName() != null ? construction.getConstructionName() : "");
        edtYear_built.setText(construction.getYearBuilt() != null ? construction.getYearBuilt().toString() : "");
        edtLocation.setText(construction.getLocation() != null ? construction.getLocation() : "");
        edtGateType.setText(construction.getGateType() != null ? construction.getGateType() : "");
        edtGateCount.setText(construction.getGateCount() != null ? construction.getGateCount().toString() : "");
        edtGateSize.setText(construction.getGateSize() != null ? construction.getGateSize().toString() : "");
        edtDesignedFlow.setText(construction.getDesignFlow() != null ? construction.getDesignFlow().toString() : "");
        edtDesignedWaterLevel.setText(construction.getDesignWaterLevel() != null ? construction.getDesignWaterLevel().toString() : "");
        edtBottomElevation.setText(construction.getBottomElevation() != null ? construction.getBottomElevation().toString() : "");
        edtWaterGaugeType.setText(construction.getWaterGaugeType() != null ? construction.getWaterGaugeType() : "");

        btnCreate1 = findViewById(R.id.btnCreate1);
        btnUpdate1 = findViewById(R.id.btnUpdate1);
        btnDelete1 = findViewById(R.id.btnDelete1);

        // Disable Create button if construction already has an ID (meaning it's an existing record)
        if (construction.getId() != null && !construction.getId().isEmpty()) {
            btnCreate1.setEnabled(false);
            btnCreate1.setAlpha(0.5F);
            btnUpdate1.setEnabled(true); // Ensure update is enabled for existing
            btnUpdate1.setAlpha(1.0F);
            btnDelete1.setEnabled(true); // Ensure delete is enabled for existing
            btnDelete1.setAlpha(1.0F);
        } else {
            // New construction, disable update/delete initially
            btnUpdate1.setEnabled(false);
            btnUpdate1.setAlpha(0.5F);
            btnDelete1.setEnabled(false);
            btnDelete1.setAlpha(0.5F);
            btnSave.setAlpha(0.5F); // Vô hiệu hóa nút Save DailyWaterLevel khi chưa có Construction ID
            btnSave.setEnabled(false);
        }

        btnCreate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kiểm tra xem đã có công trình hiện tại chưa
                if (construction.getId() != null && !construction.getId().isEmpty()) {
                    Toast.makeText(DetailActivity.this, "Công trình đã tồn tại. Vui lòng cập nhật hoặc xóa.", Toast.LENGTH_SHORT).show();
                    return;
                }
                matchData();
                construction.setId(refConstructions.push().getKey()); // Tạo ID mới
                saveConstructionData();
            }
        });

        btnUpdate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (construction.getId() == null || construction.getId().isEmpty()) {
                    Toast.makeText(DetailActivity.this, "Không có công trình để cập nhật. Vui lòng tạo mới trước.", Toast.LENGTH_SHORT).show();
                    return;
                }
                matchData();
                saveConstructionData();
            }
        });

        btnDelete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (construction.getId() == null || construction.getId().isEmpty()) {
                    Toast.makeText(DetailActivity.this, "Không có công trình để xóa.", Toast.LENGTH_SHORT).show();
                    return;
                }
                deleteConstructionData();
            }
        });
    }

    public void matchData() {
        construction.setConstructionName(edtComstructName.getText().toString().trim());
        construction.setYearBuilt(parseInt(edtYear_built));
        construction.setLocation(edtLocation.getText().toString().trim());
        // construction.setType(parseInt(edtGateType)); // edtGateType là EditText, cần xử lý để lấy giá trị số nếu Type là số
        construction.setGateCount(parseInt(edtGateCount));
        construction.setGateSize(parseDouble(edtGateSize));
        construction.setDesignFlow(parseDouble(edtDesignedFlow));
        construction.setDesignWaterLevel(parseDouble(edtDesignedWaterLevel));
        construction.setBottomElevation(parseDouble(edtBottomElevation));
        construction.setWaterGaugeType(edtWaterGaugeType.getText().toString().trim());
    }

    public void saveConstructionData() {
        // Kiểm tra kết nối mạng trước khi lưu Construction
        if (!isNetworkConnected()) {
            Toast.makeText(this, "Không có kết nối mạng. Dữ liệu công trình sẽ được lưu offline và đồng bộ khi có mạng trở lại.", Toast.LENGTH_LONG).show();
            refConstructions.child(construction.getId()).setValue(construction);
            // Kích hoạt các nút sau khi tạo thành công (offline)
            btnDelete.setEnabled(false); // Vô hiệu hóa nút xóa DailyWaterLevel khi tạo mới Construction
            btnDelete.setAlpha(0.5F);
            btnSave.setAlpha(1.0F);
            btnSave.setEnabled(true);
            btnUpdate1.setEnabled(true);
            btnUpdate1.setAlpha(1.0F);
            btnDelete1.setEnabled(true);
            btnDelete1.setAlpha(1.0F);
            btnCreate1.setEnabled(false); // Vô hiệu hóa nút Create sau khi tạo
            btnCreate1.setAlpha(0.5F);
            return;
        }

        refConstructions.child(construction.getId()).setValue(construction)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Dữ liệu công trình đã được lưu thành công!", Toast.LENGTH_SHORT).show();
                        // Kích hoạt các nút sau khi tạo thành công (online)
                        btnDelete.setEnabled(false); // Vô hiệu hóa nút xóa DailyWaterLevel khi tạo mới Construction
                        btnDelete.setAlpha(0.5F);
                        btnSave.setAlpha(1.0F);
                        btnSave.setEnabled(true);
                        btnUpdate1.setEnabled(true);
                        btnUpdate1.setAlpha(1.0F);
                        btnDelete1.setEnabled(true);
                        btnDelete1.setAlpha(1.0F);
                        btnCreate1.setEnabled(false); // Vô hiệu hóa nút Create sau khi tạo
                        btnCreate1.setAlpha(0.5F);
                    } else {
                        Toast.makeText(this, "Lưu dữ liệu công trình thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void deleteConstructionData() {
        String deletedConstructionId = construction.getId();
        if (deletedConstructionId == null || deletedConstructionId.isEmpty()) {
            Toast.makeText(this, "Không có công trình để xóa.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkConnected()) {
            Toast.makeText(this, "Không có kết nối mạng. Thao tác xóa công trình sẽ được thực hiện khi có mạng trở lại.", Toast.LENGTH_LONG).show();
            refConstructions.child(deletedConstructionId).removeValue();
            // Xóa các dailywaterlevel liên quan khỏi cache cục bộ (nếu có, và sẽ đồng bộ sau)
            Query dailyWaterLevelsToDelete = refDailyWaterLevel.orderByChild("constructionId").equalTo(deletedConstructionId);
            dailyWaterLevelsToDelete.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        child.getRef().removeValue(); // Xóa DailyWaterLevel
                        refDailyWaterLevelIds.child(child.getKey()).removeValue(); // Xóa ID tương ứng
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DetailActivity", "Lỗi khi xóa DailyWaterLevel liên quan offline: " + error.getMessage());
                }
            });

            clearFields();
            construction = null;
            dwl = null;
            btnUpdate1.setEnabled(false);
            btnUpdate1.setAlpha(0.5F);
            btnDelete.setEnabled(false);
            btnDelete.setAlpha(0.5F);
            btnDelete1.setEnabled(false);
            btnDelete1.setAlpha(0.5F);
            btnSave.setAlpha(0.5F);
            btnSave.setEnabled(false);
            return;
        }

        refConstructions.child(deletedConstructionId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Công trình đã được xóa thành công!", Toast.LENGTH_SHORT).show();
                        // TODO: Cần xem xét xóa các dailywaterlevel liên quan đến construction này
                        // Đây là phần quan trọng: khi xóa một công trình, bạn có thể muốn xóa tất cả các bản ghi dailywaterlevel liên quan
                        // Điều này cần được thực hiện trên server hoặc thông qua các thao tác xóa riêng biệt
                        Query dailyWaterLevelsToDelete = refDailyWaterLevel.orderByChild("constructionId").equalTo(deletedConstructionId);
                        dailyWaterLevelsToDelete.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    child.getRef().removeValue(); // Xóa DailyWaterLevel
                                    refDailyWaterLevelIds.child(child.getKey()).removeValue(); // Xóa ID tương ứng
                                }
                                Log.d("DetailActivity", "Đã xóa " + snapshot.getChildrenCount() + " DailyWaterLevel liên quan.");
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("DetailActivity", "Lỗi khi xóa DailyWaterLevel liên quan: " + error.getMessage());
                            }
                        });

                        clearFields();
                        construction = null; // Đặt lại construction về null
                        dwl = null; // Đặt lại dwl về null

                        btnUpdate1.setEnabled(false);
                        btnUpdate1.setAlpha(0.5F);
                        btnDelete.setEnabled(false);
                        btnDelete.setAlpha(0.5F);
                        btnDelete1.setEnabled(false);
                        btnDelete1.setAlpha(0.5F);
                        btnSave.setAlpha(0.5F);
                        btnSave.setEnabled(false);
                    } else {
                        Toast.makeText(this, "Xóa công trình thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void clearFields() {
        clearDailyWaterLevelFields();
        edtComstructName.setText("");
        edtYear_built.setText("");
        edtLocation.setText("");
        edtGateType.setText("");
        edtGateCount.setText("");
        edtGateSize.setText("");
        edtDesignedFlow.setText("");
        edtDesignedWaterLevel.setText("");
        edtBottomElevation.setText("");
        edtWaterGaugeType.setText("");
        // Sau khi xóa công trình, bật lại nút Create Construction
        btnCreate1.setEnabled(true);
        btnCreate1.setAlpha(1.0F);
    }

    private void clearDailyWaterLevelFields() {
        edtWater_level_7h.setText("");
        edtWater_level_19h.setText("");
        edtGate_open_height.setText("");
        edtOpened_gate_count.setText("");
        edtWaterFlow.setText("");
        edtNote.setText("");
    }

    public void EnterImageActivity(View view) {
        ViewParent parent = view.getParent();
        while (parent != null && !(parent instanceof CardView)) {
            parent = parent.getParent();
        }

        if (parent != null) {
            CardView cardView = (CardView) parent;

            if (cardView.getId() == R.id.cvWater_level_7h) {
                currentEditText = edtWater_level_7h;
            } else if (cardView.getId() == R.id.cvWater_level_19h) {
                currentEditText = edtWater_level_19h;
            }
            Log.d("current edit text", "EnterImageActivity: " + (currentEditText != null ? currentEditText.getId() : "null"));
        }

        Intent intent = new Intent(DetailActivity.this, ImageInputActivity.class);
        startActivityForResult(intent, 99);
    }

    private void getDailyWaterLevelById(String constructionId, String date, DailyWaterLevelCallback callback) {
        String combinedId = (constructionId + "_" + date).replace("/", "-");
        Query query = refDailyWaterLevel.orderByChild("id").equalTo(combinedId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        DailyWaterLevel waterLevel = child.getValue(DailyWaterLevel.class);
                        callback.onResult(waterLevel);
                    }
                } else {
                    callback.onResult(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    private Double parseDouble(EditText editText) {
        String input = editText.getText().toString().trim();
        if (input.isEmpty()) return null;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(EditText editText) {
        String input = editText.getText().toString().trim();
        if (input.isEmpty()) return null;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Result Code", resultCode + "");
        if (resultCode == RESULT_OK) {
            String returnedData = data.getStringExtra("RETURN_DATA");
            Log.d("returnedData from ImageInputActivity", returnedData);

            if (currentEditText != null) {
                currentEditText.setText(returnedData);
                currentEditText = null;
            }
        }
    }
}

interface DailyWaterLevelCallback {
    void onResult(@Nullable DailyWaterLevel result);
    void onError(Exception e);
}