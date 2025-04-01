package com.thuan.myapp;

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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;

public class ImageInputActivity extends AppCompatActivity {

    private final int IMAGE_PICK = 100;
    ImageView imageView;
    Bitmap bitmap;
    Yolov5TFLiteDetector yolov5TFLiteDetector;
    Paint boxPaint = new Paint();
    Paint textPain = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_input);

        imageView = findViewById(R.id.imageView);

        yolov5TFLiteDetector = new Yolov5TFLiteDetector();
        yolov5TFLiteDetector.setModelFile("best-fp16.tflite");
        yolov5TFLiteDetector.initialModel(this);

        boxPaint.setStrokeWidth(5);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.RED);

        textPain.setTextSize(50);
        textPain.setColor(Color.GREEN);
        textPain.setStyle(Paint.Style.FILL);
    }

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
            if (croppedBitmap!= null) {
                // Gọi hàm để phân tích và và kết quả
//            predict(croppedBitmap);
            }

            imageView.setImageBitmap(mutableBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error during prediction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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