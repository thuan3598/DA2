package com.thuan.myapp.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat; // Sử dụng SimpleDateFormat từ icu.text nếu muốn, nhưng java.text.SimpleDateFormat phổ biến hơn
import android.os.Bundle;
import android.util.Log; // Đảm bảo import Log

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
import android.widget.Toast; // Thêm import Toast

public class ChartActivity extends BaseActivity {

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
        if(tvHeaderTitle != null){
            tvHeaderTitle.setText(R.string.statistic);
        }
        waterLevelsList = new ArrayList<>();
        // loadDailyWaterLevels() sẽ được gọi sau khi nhận intent data
        // để đảm bảo construction, startDate, endDate, chartType đã có giá trị

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        construction = (Construction) intent.getSerializableExtra("construction");
        startDate = intent.getStringExtra("startDate");
        endDate = intent.getStringExtra("endDate");
        chartType = intent.getStringExtra("chartType");

        Log.d("ChartActivity", "Construction: " + (construction != null ? construction.getConstructionName() : "null"));
        Log.d("ChartActivity", "Start Date: " + startDate);
        Log.d("ChartActivity", "End Date: " + endDate);
        Log.d("ChartActivity", "Chart Type: " + chartType);

        lineChart = findViewById(R.id.chart);

        // Gọi hàm tải dữ liệu sau khi đã nhận được các tham số từ Intent
        loadDailyWaterLevels();
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

        // Log để kiểm tra phạm vi trục Y sau khi reset
        Log.d("ChartActivity", "Y-Axis Left Minimum (after reset): " + yAxis.getAxisMinimum());
        Log.d("ChartActivity", "Y-Axis Left Maximum (after reset): " + yAxis.getAxisMaximum());
    }

    private void drawChart() {
        // Đảm bảo construction không null và waterLevelsList không rỗng
        if (construction == null || waterLevelsList.isEmpty() || startDate == null || endDate == null || chartType == null) {
            Log.e("ChartActivity", "Không đủ dữ liệu để vẽ biểu đồ. Construction, waterLevelsList, startDate, endDate, hoặc chartType bị thiếu.");
            Toast.makeText(this, "Không đủ dữ liệu để vẽ biểu đồ.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> stats = getWaterLevelStatistics(
                construction, waterLevelsList, startDate, endDate, chartType);

        // Log các danh sách giá trị đã tính toán
        List<Double> averages = (List<Double>) stats.get("averages");
        List<Double> maxValues = (List<Double>) stats.get("max");
        List<Double> minValues = (List<Double>) stats.get("min");

        Log.d("ChartData", "Averages: " + averages);
        Log.d("ChartData", "Max Values: " + maxValues);
        Log.d("ChartData", "Min Values: " + minValues);

        // Tạo danh sách nhãn trục X
        List<String> xLabels = generateXLabels();
        Log.d("ChartData", "X-Axis Labels: " + xLabels);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setLabelCount(xLabels.size(), true);

        // Tạo dữ liệu cho đồ thị
        List<Entry> avgEntries = new ArrayList<>();
        List<Entry> maxEntries = new ArrayList<>();
        List<Entry> minEntries = new ArrayList<>();

        for (int i = 0; i < averages.size(); i++) {
            float avg = averages.get(i) != null ? averages.get(i).floatValue() : 0f;
            float max = maxValues.get(i) != null ? maxValues.get(i).floatValue() : 0f;
            float min = minValues.get(i) != null ? minValues.get(i).floatValue() : 0f;

            avgEntries.add(new Entry(i, avg));
            maxEntries.add(new Entry(i, max));
            minEntries.add(new Entry(i, min));

            // Log từng điểm dữ liệu được thêm vào biểu đồ
            Log.d("ChartEntry", "Index: " + i + ", Avg: " + avg + ", Max: " + max + ", Min: " + min + ", Label: " + xLabels.get(i));
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

        // Tự động điều chỉnh trục Y (đã được reset trong setupChart, nhưng có thể gọi lại nếu muốn)
        // lineChart.getAxisLeft().resetAxisMinimum();
        // lineChart.getAxisLeft().resetAxisMaximum();

        lineChart.invalidate(); // Làm mới biểu đồ
        lineChart.animateY(1000); // Tạo hiệu ứng động
    }

    private void loadDailyWaterLevels() {
        dailyWaterLevelDAO = new DailyWaterLevelDAOImpl();
        dailyWaterLevelDAO.loadListDailyWaterLevel(new DailyWaterLevelLoadCallback() {

            @Override
            public void onDailyWaterLevelsLoaded(List<DailyWaterLevel> DailyWaterLevels) {
                waterLevelsList.clear();
                waterLevelsList.addAll(DailyWaterLevels);

                Log.d("DailyWaterLevelDAO", "Dữ liệu mực nước thô đã tải: " + waterLevelsList.size() + " bản ghi.");
                // Log chi tiết từng bản ghi thô (có thể rất nhiều, chỉ dùng khi debug)
                // for (DailyWaterLevel level : waterLevelsList) {
                //     Log.d("DailyWaterLevelDAO", "Bản ghi thô: " + level.toString());
                // }

                // Sau khi dữ liệu được tải, gọi setupChart và drawChart
                setupChart();
                drawChart();
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ChartActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("ChartActivity", "Lỗi tải dữ liệu mực nước: " + errorMessage);
            }
        });
    }

    private List<String> generateXLabels() {
        List<String> labels = new ArrayList<>();

        try {
            // Sử dụng java.text.SimpleDateFormat thay vì android.icu.text.SimpleDateFormat để tránh lỗi tương thích
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

            switch (chartType) {
                case TYPE_DAILY:
                    java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("dd/MM", Locale.getDefault());
                    while (!calendar.getTime().after(end)) {
                        labels.add(dayFormat.format(calendar.getTime()));
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    break;

                case TYPE_MONTHLY:
                    java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MM/yyyy", Locale.getDefault());
                    while (!calendar.getTime().after(end)) {
                        labels.add(monthFormat.format(calendar.getTime()));
                        calendar.add(Calendar.MONTH, 1);
                    }
                    break;

                case TYPE_YEARLY:
                    java.text.SimpleDateFormat yearFormat = new java.text.SimpleDateFormat("yyyy", Locale.getDefault());
                    while (!calendar.getTime().after(end)) {
                        labels.add(yearFormat.format(calendar.getTime()));
                        calendar.add(Calendar.YEAR, 1);
                    }
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ChartActivity", "Lỗi phân tích ngày khi tạo nhãn X: " + e.getMessage());
            // Nếu có lỗi, tạo nhãn đơn giản theo số thứ tự (hoặc xử lý khác tùy ý)
            if (waterLevelsList != null && !waterLevelsList.isEmpty()) {
                for (int i = 0; i < waterLevelsList.size(); i++) {
                    labels.add(String.valueOf(i + 1));
                }
            } else {
                // Fallback nếu không có dữ liệu
                labels.add("N/A");
            }
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
        // Khởi tạo các danh sách rỗng để tránh NullPointerException khi trả về
        result.put("averages", new ArrayList<Double>());
        result.put("max", new ArrayList<Double>());
        result.put("min", new ArrayList<Double>());


        try {
            // Parse và validate ngày
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            if (startDate.after(endDate)) {
                Log.e("ChartActivity", "Ngày bắt đầu phải trước ngày kết thúc.");
                return result; // Trả về kết quả rỗng
            }

            // Lọc các bản ghi
            List<DailyWaterLevel> filteredLevels = filterWaterLevelsByDateAndConstruction(
                    construction, allWaterLevels, startDate, endDate);

            Log.d("ChartActivity", "Số lượng bản ghi sau khi lọc: " + filteredLevels.size());
            // Log chi tiết các bản ghi đã lọc
            // for (DailyWaterLevel level : filteredLevels) {
            //     Log.d("ChartActivity", "Bản ghi đã lọc: " + level.toString());
            // }

            if (filteredLevels.isEmpty()) {
                Log.w("ChartActivity", "Không có dữ liệu mực nước nào sau khi lọc.");
                return result; // Trả về kết quả rỗng nếu không có dữ liệu
            }

            // Kiểm tra điều kiện ngày tháng năm (cần cẩn thận với logic này)
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);

            int startYear = startCal.get(Calendar.YEAR);
            int endYear = endCal.get(Calendar.YEAR);
            int startMonth = startCal.get(Calendar.MONTH);
            int endMonth = endCal.get(Calendar.MONTH);

            // Xác định type thực tế nếu type là auto (hiện tại không có type "auto", chỉ có daily/monthly/yearly)
            String actualType = type;
            // Logic kiểm tra này có thể gây ra lỗi nếu bạn chọn khoảng thời gian không phù hợp với type
            // Ví dụ: chọn daily nhưng khoảng thời gian là cả năm sẽ không bị lỗi ở đây mà chỉ ở generateXLabels
            // Cần xem xét lại logic này nếu nó gây ra lỗi không mong muốn
            if (TYPE_YEARLY.equals(type) && startYear == endYear) {
                // throw new IllegalArgumentException("Cannot calculate yearly when start and end year are the same");
                Log.w("ChartActivity", "Cảnh báo: Đang tính toán theo năm nhưng ngày bắt đầu và kết thúc cùng năm.");
            }
            if (TYPE_MONTHLY.equals(type) && (startYear != endYear || startMonth == endMonth)) {
                // throw new IllegalArgumentException("Cannot calculate monthly when years are different or months are the same");
                Log.w("ChartActivity", "Cảnh báo: Đang tính toán theo tháng nhưng khoảng thời gian không phù hợp.");
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
                    Log.e("ChartActivity", "Loại biểu đồ không hợp lệ: " + type);
                    // throw new IllegalArgumentException("Invalid type: " + type);
            }

        } catch (ParseException e) {
            Log.e("ChartActivity", "Lỗi phân tích ngày trong getWaterLevelStatistics: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Log.e("ChartActivity", "Lỗi đối số trong getWaterLevelStatistics: " + e.getMessage());
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
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Định dạng ID: dd-MM-yyyy

        for (DailyWaterLevel level : allWaterLevels) {
            if (construction != null && !level.getConstructionId().equals(construction.getId())) {
                continue;
            }

            // Cần đảm bảo ID có định dạng "constructionId_dd-MM-yyyy"
            String[] idParts = level.getId().split("_");
            if (idParts.length < 2) {
                Log.w("ChartActivity", "ID DailyWaterLevel không hợp lệ khi lọc: " + level.getId());
                continue;
            }
            Date recordDate = sdf.parse(idParts[1]); // Lấy phần ngày "dd-MM-yyyy" từ ID

            // So sánh ngày (bao gồm cả ngày bắt đầu và ngày kết thúc)
            if (!recordDate.before(startDate) && !recordDate.after(endDate)) {
                filtered.add(level);
            }
        }
        return filtered;
    }

    private static double calculateDailyWaterLevel(DailyWaterLevel level) {
        if (level == null) return 0.0; // Tránh NullPointerException

        // Ưu tiên AvgWaterLevel nếu có
        if (level.getAvgWaterLevel() != null) {
            return level.getAvgWaterLevel();
        }

        double sum = 0;
        int count = 0;

        if (level.getWaterLevel7h() != null) { sum += level.getWaterLevel7h(); count++; }
        if (level.getWaterLevel19h() != null) { sum += level.getWaterLevel19h(); count++; }

        return count > 0 ? sum / count : 0.0; // Trả về 0.0 nếu không có dữ liệu nào
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
            boolean hasValue = false;
            if (level.getWaterLevel7h() != null) { max = Math.max(max, level.getWaterLevel7h()); hasValue = true; }
            if (level.getWaterLevel19h() != null) { max = Math.max(max, level.getWaterLevel19h()); hasValue = true; }
            if (level.getAvgWaterLevel() != null) { max = Math.max(max, level.getAvgWaterLevel()); hasValue = true; } // Bao gồm AvgWaterLevel

            maxValues.add(hasValue ? max : 0.0); // Trả về 0.0 nếu không có giá trị nào
        }
        return maxValues;
    }

    private static List<Double> calculateDailyMin(List<DailyWaterLevel> levels) {
        List<Double> minValues = new ArrayList<>();
        for (DailyWaterLevel level : levels) {
            double min = Double.MAX_VALUE;
            boolean hasValue = false;
            if (level.getWaterLevel7h() != null) { min = Math.min(min, level.getWaterLevel7h()); hasValue = true; }
            if (level.getWaterLevel19h() != null) { min = Math.min(min, level.getWaterLevel19h()); hasValue = true; }
            if (level.getAvgWaterLevel() != null) { min = Math.min(min, level.getAvgWaterLevel()); hasValue = true; } // Bao gồm AvgWaterLevel

            minValues.add(hasValue ? min : 0.0); // Trả về 0.0 nếu không có giá trị nào
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
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Định dạng ID: dd-MM-yyyy
        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MM/yyyy", Locale.getDefault());

        for (DailyWaterLevel level : levels) {
            String[] idParts = level.getId().split("_");
            if (idParts.length < 2) {
                Log.w("ChartActivity", "ID DailyWaterLevel không hợp lệ khi nhóm theo tháng: " + level.getId());
                continue;
            }
            Date date = sdf.parse(idParts[1]); // Lấy phần ngày "dd-MM-yyyy" từ ID
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
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Định dạng ID: dd-MM-yyyy
        java.text.SimpleDateFormat yearFormat = new java.text.SimpleDateFormat("yyyy", Locale.getDefault());

        for (DailyWaterLevel level : levels) {
            String[] idParts = level.getId().split("_");
            if (idParts.length < 2) {
                Log.w("ChartActivity", "ID DailyWaterLevel không hợp lệ khi nhóm theo năm: " + level.getId());
                continue;
            }
            Date date = sdf.parse(idParts[1]); // Lấy phần ngày "dd-MM-yyyy" từ ID
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
            if (values.isEmpty()) {
                averages.add(0.0); // Tránh chia cho 0 nếu nhóm rỗng
                continue;
            }
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
            if (values.isEmpty()) {
                maxValues.add(0.0); // Trả về 0.0 nếu nhóm rỗng
                continue;
            }
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
            if (values.isEmpty()) {
                minValues.add(0.0); // Trả về 0.0 nếu nhóm rỗng
                continue;
            }
            double min = Double.MAX_VALUE;
            for (Double value : values) {
                min = Math.min(min, value);
            }
            minValues.add(min);
        }
        return minValues;
    }
}