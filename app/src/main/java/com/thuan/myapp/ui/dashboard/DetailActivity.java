package com.thuan.myapp.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.thuan.myapp.data.model.Construction;
import com.thuan.myapp.data.model.DailyWaterLevel;
import com.thuan.myapp.R;

public class DetailActivity extends AppCompatActivity {
    TabHost tabHost;
    CardView cvWater_level_7h_tl, cvWater_level_7h_hl,
            cvWater_level_19h_tl, cvWater_level_19h_hl,
            cvSuction_tank_7h, cvDischarge_tank_7h,
            cvSuction_tank_19h, cvDischarge_tank_19h,
            cvAvg_water_level, cvGate_open_height,
            cvOpened_gate_count, cvWaterFlow,
            cvNote, cvPumpOperationStatus;
    EditText edtDate, edtWater_level_7h_tl, edtWater_level_7h_hl, edtWater_level_19h_tl, edtWater_level_19h_hl,
            edtSuction_tank_7h, edtDischarge_tank_7h, edtSuction_tank_19h, edtDischarge_tank_19h, edtAvg_water_level,
            edtGate_open_height, edtOpened_gate_count, edtWaterFlow, edtNote, edtPumpOperationStatus,
            edtComstructName, edtYear_built, edtLocation, edtGateType, edtGateCount, edtGateSize,
            edtDesignedFlow, edtDesignedWaterLevel, edtBottomElevation, edtWaterGaugeType;
    Button btnSave, btnDelete, btnCreate1, btnUpdate1, btnDelete1;
    DailyWaterLevel dwl = null;
    Construction construction;
    String date;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("dailywaterLevel");
    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("constructions");


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



        construction = (Construction) getIntent().getSerializableExtra("construction");
        date = getIntent().getStringExtra("date");


        getDailyWaterLevelById(construction.getId(), date, new DailyWaterLevelCallback() {
            @Override
            public void onResult(@Nullable DailyWaterLevel result) {
                if (result != null) {
                    Log.d("Firebase", "Tìm thấy bản ghi: " + result.getWaterFlow());
                    dwl = result;
                    Log.d("dwl1",dwl.getConstructionId());
                    initDailyWaterData();


                } else {
                    Log.d("Firebase", "Không tìm thấy bản ghi");
                    initDailyWaterData();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Firebase", "Lỗi khi truy vấn", e);
                initDailyWaterData();
            }
        });

        initConstructionData();




    }

    public void initDailyWaterData(){
        cvWater_level_7h_tl = findViewById(R.id.cvWater_level_7h_tl);
        cvWater_level_7h_hl = findViewById(R.id.cvWater_level_7h_hl);
        cvWater_level_19h_tl = findViewById(R.id.cvWater_level_19h_tl);
        cvWater_level_19h_hl = findViewById(R.id.cvWater_level_19h_hl);
        cvSuction_tank_7h = findViewById(R.id.cvSuction_tank_7h);
        cvDischarge_tank_7h = findViewById(R.id.cvDischarge_tank_7h);
        cvSuction_tank_19h = findViewById(R.id.cvSuction_tank_19h);
        cvDischarge_tank_19h = findViewById(R.id.cvDischarge_tank_19h);
        cvAvg_water_level = findViewById(R.id.cvAvg_water_level);
        cvGate_open_height = findViewById(R.id.cvGate_open_height);
        cvOpened_gate_count = findViewById(R.id.cvOpened_gate_count);
        cvWaterFlow = findViewById(R.id.cvWaterFlow);
        cvNote = findViewById(R.id.cvNote);
        cvPumpOperationStatus = findViewById(R.id.cvPumpOperationStatus);
        edtDate = findViewById(R.id.edtDate);
        edtWater_level_7h_tl =  findViewById(R.id.edtWater_level_7h_tl);
        edtWater_level_7h_hl =  findViewById(R.id.edtWater_level_7h_hl);
        edtWater_level_19h_tl = findViewById(R.id.edtWater_level_19h_tl);
        edtWater_level_19h_hl = findViewById(R.id.edtWater_level_19h_hl);
        edtSuction_tank_7h = findViewById(R.id.edtSuction_tank_7h);
        edtDischarge_tank_7h = findViewById(R.id.edtDischarge_tank_7h);
        edtSuction_tank_19h = findViewById(R.id.edtSuction_tank_19h);
        edtDischarge_tank_19h = findViewById(R.id.edtDischarge_tank_19h);
        edtAvg_water_level = findViewById(R.id.edtAvg_water_level);
        edtGate_open_height = findViewById(R.id.edtGate_open_height) ;
        edtOpened_gate_count = findViewById(R.id.edtOpened_gate_count);
        edtWaterFlow = findViewById(R.id.edtWaterFlow);
        edtNote = findViewById(R.id.edtNote);
        edtPumpOperationStatus = findViewById(R.id.edtPumpOperationStatus);

        edtDate.setText(date);
        edtDate.setClickable(false);
        edtDate.setFocusable(false);
        edtDate.setEnabled(false);
        if(dwl != null){
            edtWater_level_7h_tl.setText(dwl.getWaterLevel7hTl().toString());
            edtWater_level_7h_hl.setText(dwl.getWaterLevel7hHl().toString());
            edtWater_level_19h_tl.setText(dwl.getWaterLevel19hTl().toString());
            edtWater_level_19h_hl.setText(dwl.getWaterLevel19hHl().toString());
            edtSuction_tank_7h.setText(dwl.getSuctionTank7h().toString());
            edtDischarge_tank_7h.setText(dwl.getDischargeTank7h().toString());
            edtSuction_tank_19h.setText(dwl.getSuctionTank19h().toString());
            edtDischarge_tank_19h.setText(dwl.getDischargeTank19h().toString());
            edtAvg_water_level.setText(dwl.getAvgWaterLevel().toString());
            edtGate_open_height.setText(dwl.getGateOpenHeight().toString());
            edtOpened_gate_count.setText(dwl.getOpenedGateCount().toString());
            edtWaterFlow.setText(dwl.getWaterFlow().toString());
            edtNote.setText(dwl.getNotes());
            edtPumpOperationStatus.setText(dwl.getPumpOperationStatus());
        }
        int selectedIndex = construction.getType();
        // Hiện đúng card được chọn
        switch (selectedIndex) {
            case 1:
            case 0:
                cvSuction_tank_7h.setVisibility(View.GONE);
                cvDischarge_tank_7h.setVisibility(View.GONE);
                cvSuction_tank_19h.setVisibility(View.GONE);
                cvDischarge_tank_19h.setVisibility(View.GONE);
                cvPumpOperationStatus.setVisibility(View.GONE);
                break;
            case 2:
                cvWater_level_7h_hl.setVisibility(View.GONE);
                cvWater_level_7h_tl.setVisibility(View.GONE);
                cvWater_level_19h_hl.setVisibility(View.GONE);
                cvWater_level_19h_tl.setVisibility(View.GONE);
                cvAvg_water_level.setVisibility(View.GONE);
                cvGate_open_height.setVisibility(View.GONE);
                cvOpened_gate_count.setVisibility(View.GONE);
                cvWaterFlow.setVisibility(View.GONE);
                cvNote.setVisibility(View.GONE);
                break;
            case 3:
                cvSuction_tank_7h.setVisibility(View.GONE);
                cvDischarge_tank_7h.setVisibility(View.GONE);
                cvSuction_tank_19h.setVisibility(View.GONE);
                cvDischarge_tank_19h.setVisibility(View.GONE);
                cvPumpOperationStatus.setVisibility(View.GONE);
                cvAvg_water_level.setVisibility(View.GONE);
                break;
            default:
                break;
        }

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        if(dwl==null){
            btnDelete.setEnabled(false);
            btnDelete.setAlpha(0.5F);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DailyWaterLevel data = new DailyWaterLevel();
                String constructionId = construction.getId(); // Lấy từ intent hoặc biến toàn cục
                String recorderId = "0"; // Tùy bạn gán hoặc lấy động

                data.setConstructionId(constructionId);
                data.setDate(date);
                data.setWaterLevel7hTl(parseDouble(edtWater_level_7h_tl));
                data.setWaterLevel7hHl(parseDouble(edtWater_level_7h_hl));
                data.setWaterLevel19hTl(parseDouble(edtWater_level_19h_tl));
                data.setWaterLevel19hHl(parseDouble(edtWater_level_19h_hl));
                data.setSuctionTank7h(parseDouble(edtSuction_tank_7h));
                data.setDischargeTank7h(parseDouble(edtDischarge_tank_7h));
                data.setSuctionTank19h(parseDouble(edtSuction_tank_19h));
                data.setDischargeTank19h(parseDouble(edtDischarge_tank_19h));
                data.setAvgWaterLevel(parseDouble(edtAvg_water_level));
                data.setGateOpenHeight(parseDouble(edtGate_open_height));
                data.setOpenedGateCount(parseInt(edtOpened_gate_count));
                data.setWaterFlow(parseDouble(edtWaterFlow));
                data.setNotes(edtNote.getText().toString().trim());
                data.setPumpOperationStatus(edtPumpOperationStatus.getText().toString().trim());
                data.setRecorderId(recorderId);

                switch (selectedIndex) {
                    case 1:
                    case 0:
                        data.setSuctionTank7h(0.0);
                        data.setDischargeTank7h(0.0);
                        data.setSuctionTank19h(0.0);
                        data.setDischargeTank19h(0.0);
                        data.setPumpOperationStatus("null");
                        break;
                    case 2:
                        data.setWaterLevel7hTl(0.0);
                        data.setWaterLevel7hHl(0.0);
                        data.setWaterLevel19hTl(0.0);
                        data.setWaterLevel19hHl(0.0);
                        data.setAvgWaterLevel(0.0);
                        data.setGateOpenHeight(0.0);
                        data.setOpenedGateCount(0);
                        data.setWaterFlow(0.0);
                        data.setNotes("null");
                        break;
                    case 3:
                        data.setSuctionTank7h(0.0);
                        data.setDischargeTank7h(0.0);
                        data.setSuctionTank19h(0.0);
                        data.setDischargeTank19h(0.0);
                        data.setPumpOperationStatus("null");
                        data.setAvgWaterLevel(0.0);
                        break;
                    default:
                        break;
                }
                if(dwl==null){
                    data.setId((constructionId + "_" + date).replace("/", "-"));
                    saveData(data);
                }else {
                    data.setId(dwl.getId());
                    saveData(data);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteData();
            }
        });




    }

    public void deleteData(){
        ref.child(dwl.getId()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                        edtWater_level_7h_tl.setText("");
                        edtWater_level_7h_hl.setText("");
                        edtWater_level_19h_tl.setText("");
                        edtWater_level_19h_hl.setText("");
                        edtSuction_tank_7h.setText("");
                        edtDischarge_tank_7h.setText("");
                        edtSuction_tank_19h.setText("");
                        edtDischarge_tank_19h.setText("");
                        edtAvg_water_level.setText("");
                        edtGate_open_height.setText("");
                        edtOpened_gate_count.setText("");
                        edtWaterFlow.setText("");
                        edtNote.setText("");
                        edtPumpOperationStatus.setText("");

                        btnDelete.setEnabled(false);
                        btnDelete.setAlpha(0.5F);

                        dwl=null;

                    } else {
                        Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void saveData(DailyWaterLevel data){
        ref.child(data.getId()).setValue(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                        btnDelete.setEnabled(true);
                        btnDelete.setAlpha(1.0F);
                    } else {
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void initConstructionData(){
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

        edtComstructName.setText(construction.getConstructionName());
        edtYear_built.setText(construction.getYearBuilt().toString());
        edtLocation.setText(construction.getLocation());
        edtGateType.setText(construction.getGateType());
        edtGateCount.setText(construction.getGateCount().toString());
        edtGateSize.setText(construction.getGateSize().toString());
        edtDesignedFlow.setText(construction.getDesignFlow().toString());
        edtDesignedWaterLevel.setText(construction.getDesignWaterLevel().toString());
        edtBottomElevation.setText(construction.getBottomElevation().toString());
        edtWaterGaugeType.setText(construction.getWaterGaugeType());

        btnCreate1 = findViewById(R.id.btnCreate1);
        btnUpdate1 = findViewById(R.id.btnUpdate1);
        btnDelete1 = findViewById(R.id.btnDelete1);

        btnCreate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matchData();
                construction.setId(ref1.push().getKey());
                saveConstructionData();
                btnDelete.setEnabled(false);
                btnDelete.setAlpha(0.5F);

                btnSave.setAlpha(1.0F);
                btnSave.setEnabled(true);

                btnUpdate1.setEnabled(true);
                btnUpdate1.setAlpha(1.0F);
            }
        });

        btnUpdate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matchData();
                saveConstructionData();
            }
        });

        btnDelete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteConstructionData();
            }
        });

    }

    public void matchData(){
        construction.setConstructionName(edtComstructName.getText().toString().trim());
        construction.setYearBuilt(parseInt(edtYear_built));
        construction.setLocation(edtLocation.getText().toString().trim());
        construction.setType(parseInt(edtGateType));
        construction.setGateCount(parseInt(edtGateCount));
        construction.setGateSize(parseDouble(edtGateSize));
        construction.setDesignFlow(parseDouble(edtDesignedFlow));
        construction.setDesignWaterLevel(parseDouble(edtDesignedWaterLevel));
        construction.setBottomElevation(parseDouble(edtBottomElevation));
        construction.setWaterGaugeType(edtWaterGaugeType.getText().toString().trim());
    }

    public void saveConstructionData(){
        ref1.child(construction.getId()).setValue(construction)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Construction data saved successfully", Toast.LENGTH_SHORT).show();
                        btnDelete1.setEnabled(true);
                        btnDelete1.setAlpha(1.0F);


                    } else {
                        Toast.makeText(this, "Failed to save construction data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteConstructionData(){
        ref1.child(construction.getId()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Construction deleted successfully", Toast.LENGTH_SHORT).show();
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
                    } else {
                        Toast.makeText(this, "Failed to delete construction data", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void clearFields(){
        edtWater_level_7h_tl.setText("");
        edtWater_level_7h_hl.setText("");
        edtWater_level_19h_tl.setText("");
        edtWater_level_19h_hl.setText("");
        edtSuction_tank_7h.setText("");
        edtDischarge_tank_7h.setText("");
        edtSuction_tank_19h.setText("");
        edtDischarge_tank_19h.setText("");
        edtAvg_water_level.setText("");
        edtGate_open_height.setText("");
        edtOpened_gate_count.setText("");
        edtWaterFlow.setText("");
        edtNote.setText("");
        edtPumpOperationStatus.setText("");

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

    }





    public void EnterImageActivity(View view){
        Intent intent = new Intent(DetailActivity.this, ImageInputActivity.class);
        startActivity(intent);
    }

    private void getDailyWaterLevelById(String constructionId, String date, DailyWaterLevelCallback callback) {
        String combinedId = (constructionId + "_" + date).replace("/", "-");
        Query query = FirebaseDatabase.getInstance().getReference("dailywaterLevel").orderByChild("id").equalTo(combinedId);

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
        if (input.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Integer parseInt(EditText editText) {
        String input = editText.getText().toString().trim();
        if (input.isEmpty()) return 0;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


}

interface DailyWaterLevelCallback {
    void onResult(@Nullable DailyWaterLevel result);
    void onError(Exception e);
}
