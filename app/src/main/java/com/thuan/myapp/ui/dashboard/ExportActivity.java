package com.thuan.myapp.ui.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.thuan.myapp.R;

import java.io.File;
import java.io.FileOutputStream;

public class ExportActivity extends AppCompatActivity {

    Button btnCreatePdf;
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

        btnCreatePdf = findViewById(R.id.btnCreatePdf);
        btnCreatePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPDFWithIText(ExportActivity.this);
            }
        });
    }


    public void createPDFWithIText(Context context) {
        Document document = new Document(PageSize.A4.rotate()); // A4 ngang
        File pdfFile = new File(context.getExternalFilesDir(null), "mucnuoc_itext.pdf");

        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Tiêu đề
            Font fontTitle = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
            Paragraph title = new Paragraph("SỔ GHI MỰC NƯỚC", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Thước đo nước kiểu: ............................................................."));
            document.add(new Paragraph("Ngày tháng bắt đầu đo: ...................................................."));
            document.add(new Paragraph("Địa điểm: ................................................................................"));
            document.add(new Paragraph("Tháng: ............ năm ............"));
            document.add(Chunk.NEWLINE);

            // Tạo bảng
            PdfPTable table = new PdfPTable(new float[]{1.2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1.5f, 1.2f, 2f, 2f});
            table.setWidthPercentage(100);

            // Thêm header
            String[] headers = {
                    "Ngày", "7h\nTL", "7h\nHL", "19h\nTL", "19h\nHL", "Đỉnh triều\nGiờ", "Đỉnh triều\nTL",
                    "Chân triều\nGiờ", "Chân triều\nTL", "Độ cao mở cống (cm)", "Số cửa mở", "Lưu lượng (m3/s)", "Ghi chú"
            };

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setMinimumHeight(40f);
                table.addCell(cell);
            }

            // Thêm dòng dữ liệu (ngày 1–21, các cột để trống)
            for (int i = 1; i <= 21; i++) {
                table.addCell(String.valueOf(i));
                for (int j = 1; j < headers.length; j++) {
                    table.addCell("");
                }
            }

            document.add(table);
            document.close();

            Toast.makeText(context, "Tạo PDF bằng iText thành công:\n" + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi tạo PDF bằng iText", Toast.LENGTH_SHORT).show();
        }
    }


}