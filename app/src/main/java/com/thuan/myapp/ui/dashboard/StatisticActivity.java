package com.thuan.myapp.ui.dashboard;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.thuan.myapp.R;
import com.thuan.myapp.data.datasource.Callback.ConstructionLoadCallback;
import com.thuan.myapp.data.datasource.Callback.DailyWaterLevelLoadCallback;
import com.thuan.myapp.data.datasource.DAO.ConstructionDAO;
import com.thuan.myapp.data.datasource.DAO.DailyWaterLevelDAO;
import com.thuan.myapp.data.datasource.Impl.ConstructionDAOImpl;
import com.thuan.myapp.data.datasource.Impl.DailyWaterLevelDAOImpl;
import com.thuan.myapp.data.model.Construction;
import com.thuan.myapp.data.model.DailyWaterLevel;
import com.thuan.myapp.ui.adapter.ConstructionAdapter1;
import com.thuan.myapp.ui.home.HomePageActivity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticActivity extends BaseActivity {

    AutoCompleteTextView autoConstruction;
    EditText edtStartDate;
    EditText edtEndDate;
    AutoCompleteTextView autoType;
    Button btnDrawChart;
    String[] items = {"daily", "monthly", "yearly"};
    ArrayAdapter<String> adapterItems;
    private ConstructionAdapter1 adapter1;
    private List<Construction> constructionList;
    private ConstructionDAO constructionDAO;



    private Construction selectedConstruction;
    public static final String TYPE_DAILY = "daily";
    public static final String TYPE_MONTHLY = "monthly";
    public static final String TYPE_YEARLY = "yearly";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(R.string.statistic);
        }


        autoConstruction = findViewById(R.id.AutoConstruction);
        edtEndDate = findViewById(R.id.edtEndDate);
        edtStartDate = findViewById(R.id.edtStartDate);
        autoType = findViewById(R.id.AutoType);
        btnDrawChart = findViewById(R.id.btnDrawChart);

        constructionDAO = new ConstructionDAOImpl();
        constructionList = new ArrayList<>();
        loadConstruction();


        adapterItems = new ArrayAdapter<>(this, R.layout.list_item, items);
        adapter1 = new ConstructionAdapter1(this, android.R.layout.simple_dropdown_item_1line, constructionList);
        autoConstruction.setAdapter(adapter1);
        autoType.setAdapter(adapterItems);
        btnDrawChart = findViewById(R.id.btnDrawChart);

        edtEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    StatisticActivity.this,
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        edtEndDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        edtStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    StatisticActivity.this,
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        edtStartDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });




        autoType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(StatisticActivity.this, "Selected:" + item, Toast.LENGTH_SHORT).show();
            }
        });

        autoConstruction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Construction item = (Construction) adapterView.getItemAtPosition(i);
                selectedConstruction = item;
                Toast.makeText(StatisticActivity.this, "Selected:" + item.getConstructionName(), Toast.LENGTH_SHORT).show();
            }
        });

        btnDrawChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try {
//                    Map<String, Object> dailyStats = getWaterLevelStatistics(
//                            selectedConstruction,
//                            dailyWaterLevelsList,
//                            edtStartDate.getText().toString(),
//                            edtEndDate.getText().toString(),
//                            TYPE_DAILY);
//
//
//
//                    // Xử lý kết quả
//                    List<Double> dailyAverages = (List<Double>) dailyStats.get("averages");
//                    List<Double> dailyMax = (List<Double>) dailyStats.get("max");
//                    List<Double> dailyMin = (List<Double>) dailyStats.get("min");
//
//                    Log.d("DailyStats", "Daily Averages: " + dailyAverages);
//                    Log.d("DailyStats", "Daily Max: " + dailyMax);
//                    Log.d("DailyStats", "Daily Min: " + dailyMin);
//
//                    // Tương tự với monthlyStats và yearlyStats
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                    // Xử lý lỗi nếu ngày không hợp lệ
//                }

                Intent intent = new Intent(StatisticActivity.this, ChartActivity.class);
                intent.putExtra("construction", selectedConstruction); // Construction được chọn
                intent.putExtra("startDate", edtStartDate.getText().toString()); // Ngày bắt đầu
                intent.putExtra("endDate", edtEndDate.getText().toString()); // Ngày kết thúc
                intent.putExtra("chartType", autoType.getText().toString()); // Loại biểu đồ
                startActivity(intent);

            }
        });

    }



    private void loadConstruction() {

        constructionDAO.loadListConstruction(new ConstructionLoadCallback() {

            @Override
            public void onConstructionsLoaded(List<Construction> constructions) {
                constructionList.clear();
                constructionList.addAll(constructions);
                adapter1.notifyDataSetChanged();

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(StatisticActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }


    public static Map<String, Object> getWaterLevelStatistics(
            Construction construction,
            List<DailyWaterLevel> allWaterLevels,
            String startDateStr,
            String endDateStr,
            String type) {

        Map<String, Object> result = new HashMap<>();

        try {
            // Parse và validate ngày
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            if (startDate.after(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date");
            }

            // Lọc các bản ghi
            List<DailyWaterLevel> filteredLevels = filterWaterLevelsByDateAndConstruction(
                    construction, allWaterLevels, startDate, endDate);

            if (filteredLevels.isEmpty()) {
                return result;
            }

            // Kiểm tra điều kiện ngày tháng năm
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);

            int startYear = startCal.get(Calendar.YEAR);
            int endYear = endCal.get(Calendar.YEAR);
            int startMonth = startCal.get(Calendar.MONTH);
            int endMonth = endCal.get(Calendar.MONTH);

            // Xác định type thực tế nếu type là auto
            String actualType = type;
            if (TYPE_YEARLY.equals(type) && startYear == endYear) {
                throw new IllegalArgumentException("Cannot calculate yearly when start and end year are the same");
            }
            if (TYPE_MONTHLY.equals(type) && (startYear != endYear || startMonth == endMonth)) {
                throw new IllegalArgumentException("Cannot calculate monthly when years are different or months are the same");
            }

            // Tính toán theo type
            switch (actualType) {
                case TYPE_DAILY:
                    result.put("averages", calculateDailyAverages(filteredLevels));
                    result.put("max", calculateDailyMax(filteredLevels));
                    result.put("min", calculateDailyMin(filteredLevels));
                    break;
                case TYPE_MONTHLY:
                    result.put("averages", calculateMonthlyAverages(filteredLevels));
                    result.put("max", calculateMonthlyMax(filteredLevels));
                    result.put("min", calculateMonthlyMin(filteredLevels));
                    break;
                case TYPE_YEARLY:
                    result.put("averages", calculateYearlyAverages(filteredLevels));
                    result.put("max", calculateYearlyMax(filteredLevels));
                    result.put("min", calculateYearlyMin(filteredLevels));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid type: " + type);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static List<DailyWaterLevel> filterWaterLevelsByDateAndConstruction(
            Construction construction,
            List<DailyWaterLevel> allWaterLevels,
            Date startDate,
            Date endDate) throws ParseException {

        List<DailyWaterLevel> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (DailyWaterLevel level : allWaterLevels) {
            if (!level.getConstructionId().equals(construction.getId())) {
                continue;
            }

            Date recordDate = sdf.parse(level.getDate());
            if (!recordDate.before(startDate) && !recordDate.after(endDate)) {
                filtered.add(level);
            }
        }

        return filtered;
    }

    private static double calculateDailyWaterLevel(DailyWaterLevel level) {
        if (level.getAvgWaterLevel() != null) {
            return level.getAvgWaterLevel();
        }

        double sum = 0;
        int count = 0;

        if (level.getWaterLevel7h() != null) { sum += level.getWaterLevel7h(); count++; }
        if (level.getWaterLevel19h() != null) { sum += level.getWaterLevel19h(); count++; }
        return count > 0 ? sum / count : 0;
    }

    // Tính toán theo ngày
    private static List<Double> calculateDailyAverages(List<DailyWaterLevel> levels) {
        List<Double> averages = new ArrayList<>();
        for (DailyWaterLevel level : levels) {
            averages.add(calculateDailyWaterLevel(level));
        }
        return averages;
    }

    private static List<Double> calculateDailyMax(List<DailyWaterLevel> levels) {
        List<Double> maxValues = new ArrayList<>();
        for (DailyWaterLevel level : levels) {
            double max = Double.MIN_VALUE;
            if (level.getWaterLevel7h() != null) max = Math.max(max, level.getWaterLevel7h());
            if (level.getWaterLevel19h() != null) max = Math.max(max, level.getWaterLevel19h());
            maxValues.add(max);
        }
        return maxValues;
    }

    private static List<Double> calculateDailyMin(List<DailyWaterLevel> levels) {
        List<Double> minValues = new ArrayList<>();
        for (DailyWaterLevel level : levels) {
            double min = Double.MAX_VALUE;
            if (level.getWaterLevel7h() != null) min = Math.min(min, level.getWaterLevel7h());
            if (level.getWaterLevel19h() != null) min = Math.min(min, level.getWaterLevel19h());
            minValues.add(min);
        }
        return minValues;
    }

    // Tính toán theo tháng
    private static List<Double> calculateMonthlyAverages(List<DailyWaterLevel> levels) throws ParseException {
        Map<String, List<Double>> monthlyData = groupByMonth(levels);
        return calculateGroupAverages(monthlyData);
    }

    private static List<Double> calculateMonthlyMax(List<DailyWaterLevel> levels) throws ParseException {
        Map<String, List<Double>> monthlyData = groupByMonth(levels);
        return calculateGroupMax(monthlyData);
    }

    private static List<Double> calculateMonthlyMin(List<DailyWaterLevel> levels) throws ParseException {
        Map<String, List<Double>> monthlyData = groupByMonth(levels);
        return calculateGroupMin(monthlyData);
    }

    // Tính toán theo năm
    private static List<Double> calculateYearlyAverages(List<DailyWaterLevel> levels) throws ParseException {
        Map<String, List<Double>> yearlyData = groupByYear(levels);
        return calculateGroupAverages(yearlyData);
    }

    private static List<Double> calculateYearlyMax(List<DailyWaterLevel> levels) throws ParseException {
        Map<String, List<Double>> yearlyData = groupByYear(levels);
        return calculateGroupMax(yearlyData);
    }

    private static List<Double> calculateYearlyMin(List<DailyWaterLevel> levels) throws ParseException {
        Map<String, List<Double>> yearlyData = groupByYear(levels);
        return calculateGroupMin(yearlyData);
    }

    // Các hàm helper
    private static Map<String, List<Double>> groupByMonth(List<DailyWaterLevel> levels) throws ParseException {
        Map<String, List<Double>> monthlyData = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

        for (DailyWaterLevel level : levels) {
            Date date = sdf.parse(level.getDate());
            String monthKey = monthFormat.format(date);

            if (!monthlyData.containsKey(monthKey)) {
                monthlyData.put(monthKey, new ArrayList<>());
            }

            monthlyData.get(monthKey).add(calculateDailyWaterLevel(level));
        }

        return monthlyData;
    }

    private static Map<String, List<Double>> groupByYear(List<DailyWaterLevel> levels) throws ParseException {
        Map<String, List<Double>> yearlyData = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        for (DailyWaterLevel level : levels) {
            Date date = sdf.parse(level.getDate());
            String yearKey = yearFormat.format(date);

            if (!yearlyData.containsKey(yearKey)) {
                yearlyData.put(yearKey, new ArrayList<>());
            }

            yearlyData.get(yearKey).add(calculateDailyWaterLevel(level));
        }

        return yearlyData;
    }

    private static List<Double> calculateGroupAverages(Map<String, List<Double>> groupData) {
        List<Double> averages = new ArrayList<>();
        for (List<Double> values : groupData.values()) {
            double sum = 0;
            for (Double value : values) {
                sum += value;
            }
            averages.add(sum / values.size());
        }
        return averages;
    }

    private static List<Double> calculateGroupMax(Map<String, List<Double>> groupData) {
        List<Double> maxValues = new ArrayList<>();
        for (List<Double> values : groupData.values()) {
            double max = Double.MIN_VALUE;
            for (Double value : values) {
                max = Math.max(max, value);
            }
            maxValues.add(max);
        }
        return maxValues;
    }

    private static List<Double> calculateGroupMin(Map<String, List<Double>> groupData) {
        List<Double> minValues = new ArrayList<>();
        for (List<Double> values : groupData.values()) {
            double min = Double.MAX_VALUE;
            for (Double value : values) {
                min = Math.min(min, value);
            }
            minValues.add(min);
        }
        return minValues;
    }
}


