package com.thuan.myapp.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.thuan.myapp.R;
import com.thuan.myapp.data.datasource.Callback.DailyWaterLevelLoadCallback;
import com.thuan.myapp.data.datasource.DAO.DailyWaterLevelDAO;
import com.thuan.myapp.data.datasource.Impl.DailyWaterLevelDAOImpl;
import com.thuan.myapp.data.model.Construction;
import com.thuan.myapp.data.model.DailyWaterLevel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private Construction construction;
    private List<DailyWaterLevel> waterLevelsList;
    private String startDate;
    private String endDate;
    private String chartType;

    private DailyWaterLevelDAO dailyWaterLevelDAO;
    public static final String TYPE_DAILY = "daily";
    public static final String TYPE_MONTHLY = "monthly";
    public static final String TYPE_YEARLY = "yearly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        waterLevelsList = new ArrayList<>();
        loadDailyWaterLevels();

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        construction = (Construction) intent.getSerializableExtra("construction");
        startDate = intent.getStringExtra("startDate");
        endDate = intent.getStringExtra("endDate");
        chartType = intent.getStringExtra("chartType");

        lineChart = findViewById(R.id.chart);

    }

    private void setupChart() {
        Description description = new Description();
        description.setText("Biểu đồ mực nước - " + chartType);
        description.setPosition(150f, 15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
    }

    private void drawChart() {
        Map<String, Object> stats = getWaterLevelStatistics(
                construction, waterLevelsList, startDate, endDate, chartType);

        List<Double> averages = (List<Double>) stats.get("averages");
        List<Double> maxValues = (List<Double>) stats.get("max");
        List<Double> minValues = (List<Double>) stats.get("min");

        // Tạo danh sách nhãn trục X
        List<String> xLabels = generateXLabels();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setLabelCount(xLabels.size(), true);

        // Tạo dữ liệu cho đồ thị
        List<Entry> avgEntries = new ArrayList<>();
        List<Entry> maxEntries = new ArrayList<>();
        List<Entry> minEntries = new ArrayList<>();

        for (int i = 0; i < averages.size(); i++) {
            avgEntries.add(new Entry(i, averages.get(i).floatValue()));
            maxEntries.add(new Entry(i, maxValues.get(i).floatValue()));
            minEntries.add(new Entry(i, minValues.get(i).floatValue()));
        }

        // Tạo dataset
        LineDataSet avgDataSet = new LineDataSet(avgEntries, "Mực nước trung bình");
        avgDataSet.setColor(Color.BLUE);
        avgDataSet.setCircleColor(Color.BLUE);
        avgDataSet.setLineWidth(2f);
        avgDataSet.setCircleRadius(4f);

        LineDataSet maxDataSet = new LineDataSet(maxEntries, "Mực nước cao nhất");
        maxDataSet.setColor(Color.RED);
        maxDataSet.setCircleColor(Color.RED);
        maxDataSet.setLineWidth(2f);
        maxDataSet.setCircleRadius(4f);

        LineDataSet minDataSet = new LineDataSet(minEntries, "Mực nước thấp nhất");
        minDataSet.setColor(Color.GREEN);
        minDataSet.setCircleColor(Color.GREEN);
        minDataSet.setLineWidth(2f);
        minDataSet.setCircleRadius(4f);

        // Kết hợp tất cả dataset
        LineData lineData = new LineData(avgDataSet, maxDataSet, minDataSet);
        lineChart.setData(lineData);

        // Tự động điều chỉnh trục Y
        lineChart.getAxisLeft().resetAxisMinimum();
        lineChart.getAxisLeft().resetAxisMaximum();

        lineChart.invalidate();
        lineChart.animateY(1000);
    }

    private void loadDailyWaterLevels() {
        dailyWaterLevelDAO = new DailyWaterLevelDAOImpl();
        dailyWaterLevelDAO.loadListDailyWaterLevel(new DailyWaterLevelLoadCallback() {

            @Override
            public void onDailyWaterLevelsLoaded(List<DailyWaterLevel> DailyWaterLevels) {
                waterLevelsList.clear();
                waterLevelsList.addAll(DailyWaterLevels);

                Log.d("DailyWaterLevelDAO", waterLevelsList.toString());
                setupChart();
                drawChart();


            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ChartActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> generateXLabels() {
        List<String> labels = new ArrayList<>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

            switch (chartType) {
                case TYPE_DAILY:
                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    while (!calendar.getTime().after(end)) {
                        labels.add(dayFormat.format(calendar.getTime()));
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    break;

                case TYPE_MONTHLY:
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                    while (!calendar.getTime().after(end)) {
                        labels.add(monthFormat.format(calendar.getTime()));
                        calendar.add(Calendar.MONTH, 1);
                    }
                    break;

                case TYPE_YEARLY:
                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                    while (!calendar.getTime().after(end)) {
                        labels.add(yearFormat.format(calendar.getTime()));
                        calendar.add(Calendar.YEAR, 1);
                    }
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // Nếu có lỗi, tạo nhãn đơn giản theo số thứ tự
//            int size = ((List<Double>) stats.get("averages")).size();
//            for (int i = 0; i < size; i++) {
//                labels.add(String.valueOf(i + 1));
//            }
        }

        return labels;
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

        if (level.getWaterLevel7hHl() != null) { sum += level.getWaterLevel7hHl(); count++; }
        if (level.getWaterLevel19hHl() != null) { sum += level.getWaterLevel19hHl(); count++; }
        if (level.getWaterLevel7hTl() != null) { sum += level.getWaterLevel7hTl(); count++; }
        if (level.getWaterLevel19hTl() != null) { sum += level.getWaterLevel19hTl(); count++; }

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
            if (level.getWaterLevel7hHl() != null) max = Math.max(max, level.getWaterLevel7hHl());
            if (level.getWaterLevel19hHl() != null) max = Math.max(max, level.getWaterLevel19hHl());
            if (level.getWaterLevel7hTl() != null) max = Math.max(max, level.getWaterLevel7hTl());
            if (level.getWaterLevel19hTl() != null) max = Math.max(max, level.getWaterLevel19hTl());
            maxValues.add(max);
        }
        return maxValues;
    }

    private static List<Double> calculateDailyMin(List<DailyWaterLevel> levels) {
        List<Double> minValues = new ArrayList<>();
        for (DailyWaterLevel level : levels) {
            double min = Double.MAX_VALUE;
            if (level.getWaterLevel7hHl() != null) min = Math.min(min, level.getWaterLevel7hHl());
            if (level.getWaterLevel19hHl() != null) min = Math.min(min, level.getWaterLevel19hHl());
            if (level.getWaterLevel7hTl() != null) min = Math.min(min, level.getWaterLevel7hTl());
            if (level.getWaterLevel19hTl() != null) min = Math.min(min, level.getWaterLevel19hTl());
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