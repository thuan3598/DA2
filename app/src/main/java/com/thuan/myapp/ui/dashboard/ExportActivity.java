package com.thuan.myapp.ui.dashboard;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ExportActivity extends BaseActivity {

    Button btnCreatePdf;
    AutoCompleteTextView autoConstruction;
    AutoCompleteTextView edtTime;
    private ConstructionAdapter1 adapter1;
    private List<Construction> constructionList;
    private ConstructionDAO constructionDAO;
    private DailyWaterLevelDAO dailyWaterLevelDAO;
    private List<DailyWaterLevel> dailyWaterLevelList;
    private Map<Integer, List<DailyWaterLevel>> monthlyWaterLevels = new HashMap<>();

    private Random random = new Random();
    private Construction selectedConstruction;
    private RadioGroup idGroup;
    private String selectedTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_export);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(R.string.export_data_title);
        }

        btnCreatePdf = findViewById(R.id.btnCreatePdf);
        autoConstruction = findViewById(R.id.AutoConstruction);
        edtTime = findViewById(R.id.edtTime);
        idGroup = findViewById(R.id.idGroup);

        constructionDAO = new ConstructionDAOImpl();
        constructionList = new ArrayList<>();

        dailyWaterLevelDAO = new DailyWaterLevelDAOImpl();
        dailyWaterLevelList = new ArrayList<>();

        loadConstruction();
        adapter1 = new ConstructionAdapter1(this, android.R.layout.simple_dropdown_item_1line, constructionList);
        autoConstruction.setAdapter(adapter1);

        btnCreatePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Gọi loadDailyWaterLevel và truyền một Runnable để tạo PDF sau khi dữ liệu tải xong
                loadDailyWaterLevel(() -> {
                    createPDF(ExportActivity.this);
                });
            }
        });

        autoConstruction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Construction item = (Construction) adapterView.getItemAtPosition(i);
                selectedConstruction = item;
                Toast.makeText(ExportActivity.this, "Selected:" + item.getConstructionName(), Toast.LENGTH_SHORT).show();
            }
        });


        // Lắng nghe sự thay đổi trong RadioGroup
        idGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Reset giá trị thời gian khi thay đổi radio button
            selectedTime = "";
            edtTime.setText("");
            updateTimeSelection(checkedId);
        });

        // Thiết lập hành vi mặc định khi khởi động
        updateTimeSelection(idGroup.getCheckedRadioButtonId());

        // Lắng nghe sự kiện chọn item trong edtTime
        edtTime.setOnItemClickListener((parent, view, position, id) -> {
            selectedTime = parent.getItemAtPosition(position).toString();
            edtTime.setText(selectedTime); // Đảm bảo giá trị hiển thị khớp với giá trị đã chọn
        });

    }

    private void updateTimeSelection(int checkedId) {
        if (checkedId == R.id.chkYear) { // Nút đầu tiên (năm)
            showYearSelection();
        } else if (checkedId == R.id.chkMounth) { // Nút thứ hai (mm/yyyy)
            showMonthYearSelection();
        }
    }

    private void showYearSelection() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 10; i <= currentYear + 10; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years);
        edtTime.setAdapter(yearAdapter);
        edtTime.setOnClickListener(v -> {
            edtTime.showDropDown();
        });
    }

    private void showMonthYearSelection() {
        List<String> monthYears = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        for (int year = currentYear - 10; year <= currentYear + 10; year++) {
            for (int month = 1; month <= 12; month++) {
                monthYears.add(String.format("%02d/%d", month, year));
            }
        }
        ArrayAdapter<String> monthYearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, monthYears);
        edtTime.setAdapter(monthYearAdapter);
        edtTime.setOnClickListener(v -> {
            edtTime.showDropDown();
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
                Toast.makeText(ExportActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thay đổi hàm này để nhận một callback
    private void loadDailyWaterLevel(final Runnable onDataLoadedCallback) {
        if (selectedConstruction == null) {
            Toast.makeText(this, "Vui lòng chọn công trình!", Toast.LENGTH_SHORT).show();
            return;
        }

        dailyWaterLevelDAO.loadDailyWaterLevelById(selectedConstruction.getId(), new DailyWaterLevelLoadCallback() {
            @Override
            public void onDailyWaterLevelsLoaded(List<DailyWaterLevel> DailyWaterLevels) {
                dailyWaterLevelList.clear();
                monthlyWaterLevels.clear(); // Xóa dữ liệu nhóm tháng cũ
                int checkedId = idGroup.getCheckedRadioButtonId();

                // Nếu không chọn thời gian cụ thể, thì thêm tất cả dữ liệu
                if (selectedTime == null || selectedTime.isEmpty()) {
                    dailyWaterLevelList.addAll(DailyWaterLevels);
                } else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM/yyyy");

                    for (DailyWaterLevel level : DailyWaterLevels) {
                        // Cần đảm bảo ID có định dạng "constructionId_dd-MM-yyyy"
                        String[] idParts = level.getId().split("_");
                        if (idParts.length < 2) {
                            Log.w("ExportActivity", "ID DailyWaterLevel không hợp lệ, bỏ qua: " + level.getId());
                            continue; // Bỏ qua nếu ID không hợp lệ
                        }

                        try {
                            Date date = dateFormat.parse(idParts[1]); // Lấy phần ngày "dd-MM-yyyy"
                            String monthYear = monthYearFormat.format(date);
                            // int month = date.getMonth() + 1; // getMonth() đã deprecated và không tin cậy

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            int month = cal.get(Calendar.MONTH) + 1;
                            int year = cal.get(Calendar.YEAR);


                            if (checkedId == R.id.chkYear) { // Lọc theo năm
                                if (String.valueOf(year).equals(selectedTime)) {
                                    dailyWaterLevelList.add(level);
                                    // monthlyWaterLevels.computeIfAbsent(month, k -> new ArrayList<>()).add(level); // Nếu cần nhóm theo tháng trong năm
                                }
                            } else if (checkedId == R.id.chkMounth) { // Lọc theo tháng/năm
                                if (monthYear.equals(selectedTime)) {
                                    Log.d("ExportActivity", "Đã thêm DailyWaterLevel: " + level.toString());
                                    dailyWaterLevelList.add(level);
                                }
                            }
                        } catch (ParseException e) {
                            Log.e("ExportActivity", "Lỗi phân tích ngày từ ID '" + level.getId() + "': " + e.getMessage());
                        }
                    }
                }

                // Khi dữ liệu đã được tải và lọc xong, gọi callback để tạo PDF
                if (onDataLoadedCallback != null) {
                    onDataLoadedCallback.run();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ExportActivity.this, "Lỗi tải dữ liệu mực nước: " + errorMessage, Toast.LENGTH_SHORT).show();
                if (onDataLoadedCallback != null) {
                    // Xử lý trường hợp lỗi tải dữ liệu, có thể thông báo cho người dùng
                    // và không tạo PDF, hoặc tạo PDF với dữ liệu rỗng.
                    // Hiện tại, ta sẽ gọi callback để tạo PDF ngay cả khi có lỗi,
                    // để người dùng biết có file được tạo ra nhưng có thể trống.
                    // Hoặc bạn có thể bỏ dòng này nếu muốn chỉ tạo PDF khi không có lỗi.
                    onDataLoadedCallback.run();
                }
            }
        });
    }

    // Đổi tên hàm này thành createPDF để rõ ràng hơn và chỉ chứa logic tạo PDF
    public void createPDF(Context context) {
        if (selectedConstruction == null || selectedTime == null || selectedTime.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn công trình và thời gian!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem danh sách dữ liệu có rỗng không
        if (dailyWaterLevelList.isEmpty()) {
            Toast.makeText(context, "Không có dữ liệu mực nước cho công trình và thời gian đã chọn để xuất PDF.", Toast.LENGTH_LONG).show();
            return;
        }


        Document document = new Document(PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        // Tạo thư mục "Documents/MyWaterLogs" trong bộ nhớ ngoài
        File documentsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MyWaterLogs");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
        }

        // Đường dẫn file PDF
        File pdfFile = new File(documentsDir, "mucnuoc_" + selectedConstruction.getConstructionName().replaceAll("\\s+", "_") + "_" + selectedTime.replaceAll("/", "_") + ".pdf");


        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Định nghĩa phông chữ hỗ trợ tiếng Việt từ assets
            // Đảm bảo tệp font 'times.ttf' thực sự nằm trong thư mục 'assets/font/' của dự án.
            BaseFont baseFont = BaseFont.createFont("assets/font/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font fontHeader = new Font(baseFont, 14, Font.BOLD);
            Font fontTitle = new Font(baseFont, 16, Font.BOLD);
            Font fontNormal = new Font(baseFont, 12, Font.NORMAL);
            Font fontBold = new Font(baseFont, 12, Font.BOLD);

            // Title
            Paragraph title = new Paragraph("SỔ GHI MỰC NƯỚC", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Kiểm tra và hiển thị thời gian theo năm hoặc tháng/năm
            int[] timeParts = extractMonthAndYear();
            if (idGroup.getCheckedRadioButtonId() == R.id.chkYear && timeParts[1] != -1) {
                document.add(new Paragraph("Năm: " + timeParts[1], fontNormal));
            } else if (idGroup.getCheckedRadioButtonId() == R.id.chkMounth && timeParts[0] != -1 && timeParts[1] != -1) {
                document.add(new Paragraph("Tháng: " + timeParts[0] + " Năm: " + timeParts[1], fontNormal));
            }
            document.add(Chunk.NEWLINE);

            // Thông tin công trình
            document.add(new Paragraph("Điểm đo: " + selectedConstruction.getConstructionName(), fontBold));
            document.add(new Paragraph("Địa điểm: " + selectedConstruction.getLocation(), fontBold));
            document.add(new Paragraph("Đơn vị quản lý: ", fontBold)); // Cần dữ liệu thực tế cho trường này
            document.add(new Paragraph("Họ và tên người đo: Nguyễn Văn A", fontBold)); // Cần dữ liệu thực tế cho trường này

            Paragraph fullTime = new Paragraph("Ngày ........ Tháng ........ Năm ........", fontNormal);
            fullTime.setAlignment(Element.ALIGN_RIGHT);
            document.add(fullTime);

            Paragraph managingUnit = new Paragraph("Phụ trách đơn vị quản lý", fontBold);
            managingUnit.setAlignment(Element.ALIGN_RIGHT);
            managingUnit.setIndentationRight(20f);
            document.add(managingUnit);
            document.add(Chunk.NEXTPAGE); // Đẩy nội dung tiếp theo sang trang mới nếu cần


            // Thông tin đặc điểm công trình
            Paragraph charTitle = new Paragraph("ĐẶC ĐIỂM CÔNG TRÌNH", fontTitle);
            charTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(charTitle);
            document.add(new Paragraph("Tên công trình: " + (selectedConstruction.getConstructionName() != null ? selectedConstruction.getConstructionName() : ""), fontNormal));
            document.add(new Paragraph("Xây dựng năm: " + (selectedConstruction.getYearBuilt() != null ? selectedConstruction.getYearBuilt() : ""), fontNormal));
            document.add(new Paragraph("Địa điểm: " + (selectedConstruction.getLocation() != null ? selectedConstruction.getLocation() : ""), fontNormal));
            document.add(new Paragraph("Kiểu cửa cống: " + (selectedConstruction.getGateType() != null ? selectedConstruction.getGateType() : ""), fontNormal));
            document.add(new Paragraph("Số cửa: " + (selectedConstruction.getGateCount() != null ? String.valueOf(selectedConstruction.getGateCount()) : ""), fontNormal));
            // Cần kiểm tra định dạng và chia tách GateSize nếu nó lưu cả chiều cao và chiều rộng trong một trường String
            document.add(new Paragraph("Kích thước cửa: " + (selectedConstruction.getGateSize() != null ? "Cao: " + selectedConstruction.getGateSize() + "m" : ""), fontNormal));
            document.add(new Paragraph("Lưu lượng thiết kế: " + (selectedConstruction.getDesignFlow() != null ? String.format("%.2f", selectedConstruction.getDesignFlow()) + "m³/s" : ""), fontNormal));
            document.add(new Paragraph("Mực nước thiết kế: " + (selectedConstruction.getDesignWaterLevel() != null ? String.format("%.2f", selectedConstruction.getDesignWaterLevel()) + "m" : ""), fontNormal));
            document.add(new Paragraph("Cao trình đáy cống: " + (selectedConstruction.getBottomElevation() != null ? String.format("%.2f", selectedConstruction.getBottomElevation()) + "m" : ""), fontNormal));
            document.add(new Paragraph("Thước đo mực nước: " + (selectedConstruction.getWaterGaugeType() != null ? selectedConstruction.getWaterGaugeType() : ""), fontNormal));
            document.add(new Paragraph("Ngày thành lập đo: ", fontNormal)); // Cần dữ liệu thực tế cho trường này

            // Hiển thị tháng/năm hiện tại hoặc tháng/năm được chọn
            int[] currentMonthYear = extractMonthAndYear();
            if (currentMonthYear[0] != -1 && currentMonthYear[1] != -1) {
                document.add(new Paragraph("Tháng: " + currentMonthYear[0] + " năm " + currentMonthYear[1], fontNormal));
            } else if (currentMonthYear[1] != -1) { // Chỉ có năm
                document.add(new Paragraph("Năm: " + currentMonthYear[1], fontNormal));
            } else {
                document.add(new Paragraph("Tháng: " + (Calendar.getInstance().get(Calendar.MONTH) + 1) + " năm " + Calendar.getInstance().get(Calendar.YEAR), fontNormal));
            }


            document.add(Chunk.NEWLINE);

            // Bảng dữ liệu mực nước
            PdfPTable table = new PdfPTable(new float[]{1.2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f); // Khoảng cách trước bảng

            String[] headers = {
                    "Ngày", "7h", "19h", "Mực Nước TB", "Chiều Cao Cửa", "Số Cửa Mở", "Lưu Lượng", "Ghi Chú"
            };

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontBold)); // Dùng fontBold cho header
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setMinimumHeight(40f);
                table.addCell(cell);
            }
            Log.d("ExportActivity", "Bắt đầu thêm dữ liệu vào bảng PDF. Số lượng bản ghi: " + dailyWaterLevelList.size());
            for (DailyWaterLevel level : dailyWaterLevelList) {
                Log.d("ExportActivity", "Đang thêm DailyWaterLevel vào PDF: " + level.toString());
                table.addCell(new Phrase(level.getDate() != null ? level.getDate() : "", fontNormal));
                table.addCell(new Phrase(level.getWaterLevel7h() != null ? String.format("%.2f", level.getWaterLevel7h()) : "", fontNormal));
                table.addCell(new Phrase(level.getWaterLevel19h() != null ? String.format("%.2f", level.getWaterLevel19h()) : "", fontNormal));
                table.addCell(new Phrase(level.getAvgWaterLevel() != null ? String.format("%.2f", level.getAvgWaterLevel()) : "", fontNormal));
                table.addCell(new Phrase(level.getGateOpenHeight() != null ? String.format("%.2f", level.getGateOpenHeight()) : "", fontNormal));
                table.addCell(new Phrase(level.getOpenedGateCount() != null ? String.valueOf(level.getOpenedGateCount()) : "", fontNormal));
                table.addCell(new Phrase(level.getWaterFlow() != null ? String.format("%.2f", level.getWaterFlow()) : "", fontNormal));
                table.addCell(new Phrase(level.getNotes() != null ? level.getNotes() : "", fontNormal));
            }

            document.add(table);
            document.close();

            Toast.makeText(context, "Tạo PDF thành công:\n" + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi tạo PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ExportActivity", "Lỗi tạo PDF: " + e.getMessage(), e);
        }
    }


    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // Tháng bắt đầu từ 0
        int year = calendar.get(Calendar.YEAR);
        return day + " tháng " + month + " năm " + year;
    }

    // Hàm tách tháng và năm từ selectedTime
    private int[] extractMonthAndYear() {
        int[] result = new int[2]; // [0] là tháng, [1] là năm, mặc định -1 nếu không hợp lệ
        result[0] = -1; // Tháng
        result[1] = -1; // Năm

        if (selectedTime == null || selectedTime.isEmpty()) {
            // Nếu selectedTime rỗng, trả về tháng và năm hiện tại
            Calendar calendar = Calendar.getInstance();
            result[0] = calendar.get(Calendar.MONTH) + 1;
            result[1] = calendar.get(Calendar.YEAR);
            return result;
        }

        int checkedId = idGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.chkMounth) {
            // Định dạng "MM/yyyy"
            String[] parts = selectedTime.split("/");
            if (parts.length == 2) {
                try {
                    result[0] = Integer.parseInt(parts[0]); // Tháng
                    result[1] = Integer.parseInt(parts[1]); // Năm
                } catch (NumberFormatException e) {
                    Log.e("ExportActivity", "Lỗi phân tích tháng/năm: " + e.getMessage());
                }
            }
        } else if (checkedId == R.id.chkYear) {
            // Định dạng là năm
            try {
                result[1] = Integer.parseInt(selectedTime); // Năm
            } catch (NumberFormatException e) {
                Log.e("ExportActivity", "Lỗi phân tích năm: " + e.getMessage());
            }
        }

        return result;
    }
}