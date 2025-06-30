package com.thuan.myapp.ui.dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.thuan.myapp.R;
import com.thuan.myapp.ai.Model;
import com.thuan.myapp.ai.Recognition;
import com.thuan.myapp.ai.Yolov5TFLiteDetector;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class ImageInputActivity extends BaseActivity {

    private final int IMAGE_PICK = 100;
    ImageView imageView, iconOverlay;
    Bitmap bitmap;
    Yolov5TFLiteDetector yolov5TFLiteDetector;
    Paint boxPaint = new Paint();
    Paint textPaint = new Paint();
    EditText edtResult;
    Button btnDone;
    private static final int imageSize = 32; // Kích thước ảnh đầu vào cho mô hình

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_input);
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(R.string.image_input);
        }

        imageView = findViewById(R.id.imageView);
        iconOverlay = findViewById(R.id.iconOverlay);
        edtResult = findViewById(R.id.edtResult);
        btnDone = findViewById(R.id.btnDone);

        yolov5TFLiteDetector = new Yolov5TFLiteDetector();
        yolov5TFLiteDetector.setModelFile("best-fp16.tflite");
        yolov5TFLiteDetector.initialModel(this);

        boxPaint.setStrokeWidth(5);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.RED);

        textPaint.setTextSize(50);
        textPaint.setColor(Color.GREEN);
        textPaint.setStyle(Paint.Style.FILL);


        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Không thể khởi tạo OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV đã được khởi tạo thành công");
        }

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent returnIntent = new Intent();
                returnIntent.putExtra("RETURN_DATA", edtResult.getText().toString());
                Log.d("return_data", edtResult.getText().toString());
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    public void selectImage(View view) {
        imageView.setImageBitmap(null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setMessage("Would you like to upload an image from your device or use the camera?")
                .setPositiveButton("Upload from device", (dialog, id) -> {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, IMAGE_PICK);
                })
                .setNegativeButton("Use camera", (dialog, id) -> {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                        return;
                    }
                    startActivityForResult(cameraIntent, 99);
                });
        builder.create().show();
    }

    public void predict(View view) {
        if (bitmap == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(bitmap);
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);

            Recognition maxBottomRecognition = null;
            Recognition secondMaxBottomRecognition = null;
            Bitmap croppedBitmap = null;

            for (Recognition recognition : recognitions) {
                if (recognition.getConfidence() > 0.4) {
                    RectF location = recognition.getLocation();
                    canvas.drawRect(location, boxPaint);
                    canvas.drawText(recognition.getLabelName() + ":" + recognition.getConfidence(), location.left, location.top, textPaint);

                    if (maxBottomRecognition == null || location.bottom > maxBottomRecognition.getLocation().bottom) {
                        secondMaxBottomRecognition = maxBottomRecognition;
                        maxBottomRecognition = recognition;
                    } else if (secondMaxBottomRecognition == null || location.bottom > secondMaxBottomRecognition.getLocation().bottom) {
                        secondMaxBottomRecognition = recognition;
                    }
                }
            }

            if (secondMaxBottomRecognition != null) {
                RectF location = secondMaxBottomRecognition.getLocation();
                int left = Math.max(0, (int) location.left);
                int top = Math.max(0, (int) location.top);
                int width = Math.min(bitmap.getWidth() - left, (int) location.width());
                int height = Math.min(bitmap.getHeight() - top, (int) location.height());
                croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
            }

            if (croppedBitmap != null) {
                // Sử dụng OCR trước
                TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
                if (!textRecognizer.isOperational()) {
                    edtResult.setText("TextRecognizer not operational");
                    Log.e("OCR", "TextRecognizer is not operational.");
                    return;
                }

                Frame frameImage = new Frame.Builder().setBitmap(croppedBitmap).build();
                SparseArray<TextBlock> textBlockSparseArray = textRecognizer.detect(frameImage);
                StringBuilder stringImageText = new StringBuilder();

                for (int i = 0; i < textBlockSparseArray.size(); i++) {
                    TextBlock textBlock = textBlockSparseArray.valueAt(i);
                    stringImageText.append(" ").append(textBlock.getValue());
                }

                String ocrResult = stringImageText.toString().trim();
                Log.d("OCR", "OCR Result: " + ocrResult);

                // Kiểm tra xem kết quả OCR có chỉ chứa số hay không
                if (isNumeric(ocrResult) && !ocrResult.isEmpty()) {
                    // Kết quả OCR chỉ chứa số, hiển thị trực tiếp
                    edtResult.setText(ocrResult);
                } else {
                    // Kết quả OCR không phải số hoặc rỗng, tách ký tự và nhận diện bằng mô hình
                    List<Bitmap> charBitmaps = segmentCharacters(croppedBitmap);
                    StringBuilder recognizedText = new StringBuilder();

                    Log.d("CharSegment", "Number of characters segmented: " + charBitmaps.size());
                    for (Bitmap charBitmap : charBitmaps) {
                        int prediction = classifyImage(charBitmap);
                        Log.d("Predict", "Predicted class: " + prediction);
                        if (prediction >= 0) {
                            // Giả sử mô hình trả về số từ 0-9, chuyển thành ký tự
                            recognizedText.append(prediction);
                        } else {
                            recognizedText.append("?");
                        }
                    }

                    edtResult.setText(recognizedText.toString());
                }

                if (maxBottomRecognition != null && secondMaxBottomRecognition != null) {
                    float V_value;
                    try {
                        V_value = Float.parseFloat(edtResult.getText().toString());

                        float y_value = secondMaxBottomRecognition.getLocation().bottom;
                        float h_value = secondMaxBottomRecognition.getLocation().height();
                        float y_gauge = maxBottomRecognition.getLocation().bottom; // y_gauge là tọa độ y của đáy vùng ảnh thước đo I_gauge
                        int deltaV = (V_value % 10 == 0 ? 10 : 1);
                        Log.d("CalculateM", "V_value: " + V_value);
                        Log.d("CalculateM", "y_value: " + y_value);
                        Log.d("CalculateM", "h_value: " + h_value);
                        Log.d("CalculateM", "y_gauge: " + y_gauge);
                        Log.d("CalculateM", "deltaV: " + deltaV);
                        // Đảm bảo không chia cho 0
                        if (h_value != 0 && deltaV != 0) {
                            float M = V_value - (-y_value + y_gauge) / (h_value / deltaV);
                            edtResult.setText(String.format("%.2f", M)); // Hiển thị giá trị M làm kết quả cuối cùng
                            Log.d("CalculateM", "Calculated M: " + M);
                        } else {
                            Log.e("CalculateM", "h_value or deltaV is zero, cannot calculate M.");
                            Toast.makeText(this, "Cannot calculate M: Height or deltaV is zero.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Log.e("CalculateM", "Error parsing V value: " + e.getMessage());
                        Toast.makeText(this, "Could not parse V value. Result might be inaccurate.", Toast.LENGTH_SHORT).show();
                        edtResult.setText("-1");
                    }


                } else {
                    Log.w("CalculateM", "Gauge (I_gauge) or value area (I_value) not fully detected, cannot calculate M.");
                    Toast.makeText(this, "Could not detect gauge or value area for M calculation.", Toast.LENGTH_SHORT).show();
                }
            }

            imageView.setImageBitmap(mutableBitmap);
            iconOverlay.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error during prediction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNumeric(String str) {
        // Kiểm tra xem chuỗi có chỉ chứa số (0-9) hay không
        if (str == null || str.isEmpty()) return false;
        return Pattern.matches("^[0-9]+$", str);
    }

    private boolean checkImageQuality(Bitmap bitmap) {
        // Chuyển Bitmap sang Mat
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

        // Kiểm tra độ mờ (Laplacian variance)
        Mat laplacian = new Mat();
        Imgproc.Laplacian(gray, laplacian, CvType.CV_64F);
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stdDev = new MatOfDouble();
        Core.meanStdDev(laplacian, mean, stdDev);
        double[] stdDevData = stdDev.toArray();
        double lapVar = stdDevData[0] * stdDevData[0];
        boolean isBlurry = lapVar < 100.0;
        Log.d("ImageQuality", "Laplacian Variance: " + lapVar);

        // Kiểm tra nhiễu (Standard deviation)
        Core.meanStdDev(gray, mean, stdDev);
        stdDevData = stdDev.toArray();
        double noiseStd = stdDevData[0];
        boolean isNoisy = noiseStd > 30.0;
        Log.d("ImageQuality", "Noise StdDev: " + noiseStd);

        // Kiểm tra góc nghiêng (Hough Transform)
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 50, 150);
        Mat lines = new Mat();
        Imgproc.HoughLines(edges, lines, 1, Math.PI / 180, 150);
        double skewAngle = 0;
        if (lines.cols() > 0) {
            List<Double> angles = new ArrayList<>();
            for (int i = 0; i < Math.min(lines.cols(), 20); i++) {
                double[] line = lines.get(0, i);
                double theta = line[1];
                double angle = (theta * 180 / Math.PI) - 90;
                angles.add(angle);
            }
            Collections.sort(angles);
            skewAngle = angles.get(angles.size() / 2); // Median
        }
        boolean isSkewed = Math.abs(skewAngle) > 5;
        Log.d("ImageQuality", "Skew Angle: " + skewAngle);

        // Giải phóng bộ nhớ
        mat.release();
        gray.release();
        laplacian.release();
        edges.release();
        lines.release();
        mean.release();
        stdDev.release();

        // Ảnh đạt chất lượng nếu không mờ, không nhiễu, và không nghiêng
        return !isBlurry && !isNoisy && !isSkewed;
    }

    private List<Bitmap> segmentCharacters(Bitmap bitmap) {
        List<Bitmap> charBitmaps = new ArrayList<>();

        // Chuyển Bitmap sang Mat
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);

        if (mat.type() == CvType.CV_8UC4) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGR);
        }

        // Tiền xử lý: Làm mịn và chuyển sang LAB
        Mat blurred = new Mat();
        Imgproc.bilateralFilter(mat, blurred, 5, 7, 7);
        Mat lab = new Mat();
        Imgproc.cvtColor(blurred, lab, Imgproc.COLOR_BGR2Lab);

        // Tách kênh L
        List<Mat> labChannels = new ArrayList<>();
        Core.split(lab, labChannels);
        Mat lChannel = labChannels.get(0);

        // Áp dụng CLAHE
        Mat claheMat = new Mat();
        org.opencv.imgproc.CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 4));
        clahe.apply(lChannel, claheMat);

        // Gộp lại kênh LAB
        labChannels.set(0, claheMat);
        Mat mergedLab = new Mat();
        Core.merge(labChannels, mergedLab);

        // Chuyển về BGR và grayscale
        Mat bgr = new Mat();
        Imgproc.cvtColor(mergedLab, bgr, Imgproc.COLOR_Lab2BGR);
        Mat gray = new Mat();
        Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY);

        // Kiểm tra màu nền
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_OTSU);
        int[] pixelCounts = new int[256];
        byte[] threshData = new byte[(int) thresh.total()];
        thresh.get(0, 0, threshData);
        for (byte b : threshData) {
            pixelCounts[b & 0xFF]++;
        }
        int bgColor = pixelCounts[255] > pixelCounts[0] ? 255 : 0;

        // Nhị phân hóa dựa trên màu nền
        Mat binary = new Mat();
        if (bgColor == 255) {
            Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        } else {
            Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        }

        // Morphology
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat morp = new Mat();
        Imgproc.morphologyEx(binary, morp, Imgproc.MORPH_OPEN, kernel);

        // Tìm contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(morp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Lọc và tách ký tự
        List<Rect> charBoxes = new ArrayList<>();
        double minArea = 100;
        double maxHeight = mat.height() * 0.5;
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.width > 5 && rect.height > 10 && rect.width * rect.height > minArea && rect.height < maxHeight) {
                charBoxes.add(rect);
            }
        }

        // Sắp xếp theo tọa độ x
        Collections.sort(charBoxes, Comparator.comparingInt(r -> r.x));

        // Tách từng ký tự
        for (Rect rect : charBoxes) {
            Mat charMat = new Mat(morp, rect);
            Bitmap charBitmap = Bitmap.createBitmap(charMat.cols(), charMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(charMat, charBitmap);
            charBitmaps.add(charBitmap);
            charMat.release();
        }

        // Giải phóng bộ nhớ
        mat.release();
        blurred.release();
        lab.release();
        lChannel.release();
        claheMat.release();
        mergedLab.release();
        bgr.release();
        gray.release();
        thresh.release();
        binary.release();
        morp.release();
        hierarchy.release();

        return charBitmaps;
    }

    private int classifyImage(Bitmap inputImage) {
        try {
            // Resize ảnh về 32x32
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(inputImage, imageSize, imageSize, true);

            // Tạo ByteBuffer
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            resizedBitmap.getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize);

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    float r = ((val >> 16) & 0xFF) / 255.f;
                    float g = ((val >> 8) & 0xFF) / 255.f;
                    float b = (val & 0xFF) / 255.f;
                    byteBuffer.putFloat(r);
                    byteBuffer.putFloat(g);
                    byteBuffer.putFloat(b);
                }
            }

            // Load model
            Model model = Model.newInstance(getApplicationContext());

            // Tạo input Tensor
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Dự đoán
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                Log.d("Confidence", "Class " + i + ": " + confidences[i]);
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            model.close();

            if (maxConfidence < 0.4f) return -1; // Ngưỡng tin cậy thấp → loại
            return maxPos;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ClassifyImage", "Error classifying image: " + e.getMessage());
            return -1;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK && data != null) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            bitmap = photo.copy(Bitmap.Config.ARGB_8888, false);
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("RETURN_DATA", edtResult.getText().toString());
        Log.d("return_data", edtResult.getText().toString());
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}