package com.thuan.myapp.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thuan.myapp.data.model.Construction;
import com.thuan.myapp.ui.dashboard.DetailActivity;
import com.thuan.myapp.ui.dashboard.ExportActivity;
import com.thuan.myapp.ui.dashboard.MapActivity;
import com.thuan.myapp.ui.dashboard.StatisticActivity;
import com.thuan.myapp.ui.adapter.ConstructionAdapter;
import com.thuan.myapp.R;

public class HomePageActivity extends AppCompatActivity {

    CardView cvNew, cvAccount, cvMap, cvStatistic, cvExportData;

    List<Construction> constructionList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loadConstructionFromFirebase();
        cvNew = findViewById(R.id.cvNew);
//        cvAccount = findViewById(R.id.cvAccount);
        cvMap = findViewById(R.id.cvMap);
        cvStatistic = findViewById(R.id.cvStatistic);
        cvExportData = findViewById(R.id.cvExportData);

        cvNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_logbook_choices, null);
                builder.setView(dialogView);

                AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteLogbook);
                EditText edtDatePicker = dialogView.findViewById(R.id.edtDatePicker);
                Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);



//                List<Construction> constructionList = getConstructionList();
                // Adapter dùng constructionList
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
                        autoCompleteTextView.setError("Please select a construction");
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
                        autoCompleteTextView.setError("Invalid construction selected");
                        autoCompleteTextView.requestFocus();
                        return;
                    }

                    if (selectedDate.isEmpty()) {
                        edtDatePicker.setError("Please select a date");
                        edtDatePicker.requestFocus();
                        return;
                    }

                    Intent intent = new Intent(HomePageActivity.this, DetailActivity.class);
                    intent.putExtra("construction", selectedConstruction);
                    intent.putExtra("date", selectedDate);
                    startActivity(intent);
                    dialog.dismiss();
                });
            }

        });

        cvMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePageActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

//        cvAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(HomePageActivity.this, AccountActivity.class);
//                startActivity(intent);
//            }
//        });

        cvStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePageActivity.this, StatisticActivity.class);
                startActivity(intent);
            }
        });

        cvExportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePageActivity.this, ExportActivity.class);
                startActivity(intent);
            }
        });



    }

    // Phương thức giả lập dữ liệu (thay thế bằng lấy từ Firebase trong thực tế)



private void loadConstructionFromFirebase() {
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("constructions");

    ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<Construction> constructions = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                Construction c = child.getValue(Construction.class);
                if (c != null) {
                    constructions.add(c);
                }
                constructionList = constructions;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(HomePageActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
        }
    });
}


}