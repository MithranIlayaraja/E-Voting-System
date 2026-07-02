package com.example.votingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.camera.core.ImageProxy;
import java.nio.ByteBuffer;


public class FaceVerificationActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private PreviewView previewView;
    private Button btnVerify;
    private TextView statusText;
    private ImageCapture imageCapture;
    private String voterId, faceUrl;
    private String boothId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_verification);

        previewView = findViewById(R.id.previewView);
        btnVerify = findViewById(R.id.btnVerifyFace);
        statusText = findViewById(R.id.statusText);

        boothId = getIntent().getStringExtra("boothId");
        voterId = getIntent().getStringExtra("VoterId");
        loadFaceUrlFromFirebase(voterId);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            startCamera();
        }

        btnVerify.setOnClickListener(v -> captureAndCompareFace());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("FaceCam", "Camera init failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndCompareFace() {
        if (imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);
                        imageProxy.close();
                        detectFace(bitmap);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(FaceVerificationActivity.this, "Capture failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void detectFace(Bitmap selfieBitmap) {
        if (faceUrl == null) {
            Toast.makeText(this, "No face registered for this voter", Toast.LENGTH_LONG).show();
            return;
        }

        // Load registered image from URL
        new Thread(() -> {
            try {
                Bitmap firebaseBitmap = BitmapFactory.decodeStream(new URL(faceUrl).openStream());
                runOnUiThread(() -> compareFaces(selfieBitmap, firebaseBitmap));
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(FaceVerificationActivity.this, "Failed to load voter image", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void compareFaces(Bitmap selfie, Bitmap firebaseImage) {
        FaceDetector detector = FaceDetection.getClient(
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .build());

        try {
            InputImage image1 = InputImage.fromBitmap(selfie, 0);
            InputImage image2 = InputImage.fromBitmap(firebaseImage, 0);

            detector.process(image1).addOnSuccessListener(faces1 -> {
                detector.process(image2).addOnSuccessListener(faces2 -> {
                    if (!faces1.isEmpty() && !faces2.isEmpty()) {
                        statusText.setText("✅ Face Verified!");
                        // Proceed to next activity
                        Intent intent = new Intent(FaceVerificationActivity.this, VotingActivity.class);
                        intent.putExtra("boothId", boothId);
                        intent.putExtra("VoterId", voterId);
                        startActivity(intent);
                    } else {
                        statusText.setText("❌ Face not recognized");
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy plane = imageProxy.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadFaceUrlFromFirebase(String voterId) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child("Voters").child(voterId).child("Face1");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                faceUrl = snapshot.getValue(String.class);
                if (faceUrl == null) {
                    statusText.setText("⚠️ No registered face found.");
                } else {
                    statusText.setText("Face data loaded, ready for verification.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                statusText.setText("Error loading face data.");
            }
        });
    }
}
