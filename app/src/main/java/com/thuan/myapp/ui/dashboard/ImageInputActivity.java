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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.thuan.myapp.ai.Model;
import com.thuan.myapp.R;
import com.thuan.myapp.ai.Recognition;
import com.thuan.myapp.ai.Yolov5TFLiteDetector;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
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

public class ImageInputActivity extends AppCompatActivity {

    private final int IMAGE_PICK = 100;
    ImageView imageView;
    Bitmap bitmap;
    Yolov5TFLiteDetector yolov5TFLiteDetector;
    Paint boxPaint = new Paint();
    Paint textPain = new Paint();

    EditText edtResult;

    int imageSize = 28;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_input);

        imageView = findViewById(R.id.imageView);
        edtResult = findViewById(R.id.edtResult);

        yolov5TFLiteDetector = new Yolov5TFLiteDetector();
        yolov5TFLiteDetector.setModelFile("best-fp16.tflite");
        yolov5TFLiteDetector.initialModel(this);

        boxPaint.setStrokeWidth(5);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.RED);

        textPain.setTextSize(50);
        textPain.setColor(Color.GREEN);
        textPain.setStyle(Paint.Style.FILL);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Không thể khởi tạo OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV đã được khởi tạo thành công");
        }    }

    public void selectImage(View view) {
        imageView.setImageBitmap(null);
        // Create an AlertDialog with two options
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setMessage("Would you like to upload an image from your device or use the camera?")
                .setPositiveButton("Upload from device", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // When "Upload from device" is selected
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, IMAGE_PICK);
                    }
                })
                .setNegativeButton("Use camera", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // When "Use camera" is selected
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        if (ActivityCompat.checkSelfPermission(ImageInputActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            // If the camera permission is not granted, request permission
                            ActivityCompat.requestPermissions(ImageInputActivity.this, new String[]{android.Manifest.permission.CAMERA}, 1);
                            return;
                        }
                        startActivityForResult(cameraIntent, 99);
                    }
                });

        // Show the dialog
        builder.create().show();
    }

    public void predict(View view){
        if(bitmap == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ArrayList<Recognition> recognitions =  yolov5TFLiteDetector.detect(bitmap);
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);

            Recognition maxBottomRecognition = null;
            Recognition secondMaxBottomRecognition = null;
            Bitmap croppedBitmap = null;


            for(Recognition recognition: recognitions){
                if(recognition.getConfidence() > 0.4){
                    RectF location = recognition.getLocation();
                    canvas.drawRect(location, boxPaint);
                    canvas.drawText(recognition.getLabelName() + ":" + recognition.getConfidence(), location.left, location.top, textPain);

                    // Cập nhật maxBottomRecognition và secondMaxBottomRecognition
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

                // Chuyển đổi tọa độ float sang int vì Bitmap.createBitmap cần giá trị int
                int left = Math.max(0, (int) location.left);
                int top = Math.max(0, (int) location.top);
                int width = Math.min(bitmap.getWidth() - left, (int) location.width());
                int height = Math.min(bitmap.getHeight() - top, (int) location.height());

                // Cắt ảnh từ bitmap gốc
                croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
            }
//            Bitmap croppedBitmap = mutableBitmap;
            if (croppedBitmap != null) {
                // Gọi hàm để phân tích và và kết quả
//            predict(croppedBitmap);
//                imageView.setImageBitmap(croppedBitmap);

                Mat mat = new Mat();
                Utils.bitmapToMat(croppedBitmap, mat);
                List<Mat> digits = extractDigitClusters(mat);

//                List<Mat> digits = extractDigits(mat);
                StringBuilder resultText = new StringBuilder();
                int i = 0;
                Log.d("digit size", digits.size()+"");
                for (Mat digit : digits) {
                    int prediction = classifyImage(digit);  // Viết lại classifyImage() để trả về số
                    Log.d("Predict", "" + prediction);
                    if(prediction >= 0){
                        resultText.append(prediction);
                    }

//                    if(i==0){
//                        Bitmap outputBitmap = Bitmap.createBitmap(digit.cols(), digit.rows(), Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(digit, outputBitmap);
//                        imageView.setImageBitmap(outputBitmap);
//                    }
                    i++;
                }
//                imageView.setImageBitmap(digits.get(1));
                edtResult.setText(resultText.toString());
                Log.d("so digit", ""+i);

            }
            imageView.setImageBitmap(mutableBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error during prediction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

//    public void classifyImage(Bitmap image){
//        try {
//            BestModel model = BestModel.newInstance(getApplicationContext());
//
//            // Creates inputs for reference.
//            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
//            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
//            byteBuffer.order(ByteOrder.nativeOrder());
//
//            int[] intValues = new int[imageSize * imageSize];
//            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
//            int pixel = 0;
//            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
//            for(int i = 0; i < imageSize; i ++){
//                for(int j = 0; j < imageSize; j++){
//                    int val = intValues[pixel++]; // RGB
//                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
//                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
//                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
//                }
//            }
//
//            inputFeature0.loadBuffer(byteBuffer);
//
//            // Runs model inference and gets result.
//            BestModel.Outputs outputs = model.process(inputFeature0);
//            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//
//            float[] confidences = outputFeature0.getFloatArray();
//            // find the index of the class with the biggest confidence.
//            int maxPos = 0;
//            float maxConfidence = 0;
//            for (int i = 0; i < confidences.length; i++) {
//                if (confidences[i] > maxConfidence) {
//                    maxConfidence = confidences[i];
//                    maxPos = i;
//                }
//            }
//            String[] classes = {"Apple", "Banana", "Orange"};
//            result.setText(classes[maxPos]);
//
//            // Releases model resources if no longer used.
//            model.close();
//        } catch (IOException e) {
//            // TODO Handle the exception
//        }
//    }

    public int classifyImage(Mat inputImage) {
        try {
            // Resize ảnh về 32x32
            Mat resizedMat = new Mat();
            Imgproc.resize(inputImage, resizedMat, new Size(32, 32));

            // Chuyển ảnh sang RGB nếu chưa đúng định dạng
            if (resizedMat.channels() != 3) {
                Imgproc.cvtColor(resizedMat, resizedMat, Imgproc.COLOR_RGBA2RGB); // hoặc COLOR_BGR2RGB tùy format gốc
            }

            // Tạo Bitmap từ Mat
            Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(resizedMat, bitmap);

            // Tạo ByteBuffer
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 32 * 32 * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[32 * 32];
            bitmap.getPixels(intValues, 0, 32, 0, 0, 32, 32);

            int pixel = 0;
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 32; j++) {
                    int val = intValues[pixel++];

                    // Lấy R, G, B
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
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Dự đoán
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                Log.d("Confidence", "" + confidences[i]);
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            model.close();

            if (maxConfidence < 0.4f) return -1;  // Ngưỡng tin cậy thấp → loại
            return maxPos;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public List<Mat> extractDigitClusters(Mat input) {
        List<Mat> digitClusters = new ArrayList<>();

        if (input == null || input.empty()) {
            Log.e("DigitCluster", "Input image is empty");
            return digitClusters;
        }

        // 1. Chuyển ảnh sang grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_BGR2GRAY);

        // 2. Làm nổi bật viền bằng Canny
        Mat edges = new Mat();
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        Imgproc.Canny(gray, edges, 50, 150);

        // 3. Mở rộng vùng viền (dilate) để nối các số gần nhau thành cụm
        Mat dilated = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.dilate(edges, dilated, kernel);

        // 4. Tìm contour các vùng
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilated, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 5. Cắt từng cụm contour ra
        List<Rect> boundingRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.height > 20 && rect.height >= input.height()/2 && rect.width > 20) { // lọc nhiễu
                boundingRects.add(rect);
            }
        }

        // 6. Sắp xếp các bounding box theo tọa độ X (thứ tự từ trái sang phải)
        Collections.sort(boundingRects, new Comparator<Rect>() {
            @Override
            public int compare(Rect r1, Rect r2) {
                return Integer.compare(r1.x, r2.x);
            }
        });

        // 7. Cắt các vùng theo boundingRect đã sắp xếp
        for (Rect rect : boundingRects) {
            Mat roi = new Mat(input, rect).clone(); // cắt cụm
            digitClusters.add(roi);
        }

        return digitClusters;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_PICK && data != null){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(requestCode == 99&&resultCode == Activity.RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            bitmap = photo.copy(Bitmap.Config.ARGB_8888, false);
            imageView.setImageBitmap(bitmap);
        }
    }
}