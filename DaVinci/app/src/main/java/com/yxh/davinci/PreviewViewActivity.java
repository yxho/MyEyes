package com.yxh.davinci;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.yxh.davinci.ImageAnalysisAbility.FaceRecognition;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PreviewViewActivity extends AppCompatActivity {
    private String TAG = "MyEyes";
    private Preview preview;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private PreviewView viewFinder;
    private ViewCanvas viewCanvas;
    private File outputDirectory;
    private ExecutorService cameraExecutor;
    private int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private FaceRecognition faceRecognition;
    private String ability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_view);
        getSupportActionBar().hide();// 隐藏标题栏
        Intent intent = getIntent();
        ability = intent.getStringExtra("ability");
        Button camera_capture_button = findViewById(R.id.camera_capture_button);
        viewFinder = findViewById(R.id.viewFinder);
        viewCanvas = findViewById(R.id.viewCanvas);
        cameraExecutor = Executors.newSingleThreadExecutor();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    PreviewViewActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        camera_capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takePhoto();
                onBackPressed();
            }
        });

    }

    private void startCamera() {
        //Future表示一个异步的任务，ListenableFuture可以监听这个任务，当任务完成的时候执行回调
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    preview = new Preview.Builder()
                            .build();

                    imageCapture = new ImageCapture.Builder()
                            //优化捕获速度，可能降低图片质量
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build();

                    imageAnalyzer = new ImageAnalysis.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)//设置长宽比与PreviewView一致
                            .build();

                    faceRecognition = new FaceRecognition(PreviewViewActivity.this);

                    imageAnalyzer.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                        @Override
                        public void analyze(@NonNull ImageProxy image) {
                            //imageAnalyzerFunc(image);
                            if (ability.equals("FaceRecognition")) {
                                faceRecognition.FindFacePosition(image, viewCanvas, viewFinder);
                            }
                            image.close();
                        }
                    });

                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    //重新绑定之前必须先取消绑定
                    cameraProvider.unbindAll();

                    camera = cameraProvider.bindToLifecycle(PreviewViewActivity.this,
                            cameraSelector, preview, imageCapture, imageAnalyzer);

                    preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        outputDirectory = getOutputDirectory();
        Log.e(TAG, String.valueOf(outputDirectory));
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(outputDirectory).build();
        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(outputDirectory);
                String msg = "Photo capture succeeded:" + savedUri;
                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                //TODO:震動，聲音，提示  为啥Toast报错
                Log.d(TAG, msg);

                MediaStore.Images.Media.insertImage(getContentResolver(), BitmapFactory.decodeFile(outputDirectory.getAbsolutePath()), outputDirectory.getName(), null);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(outputDirectory);
                intent.setData(uri);
                sendBroadcast(intent);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(PreviewViewActivity.this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    File getOutputDirectory() {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(PreviewViewActivity.this, "Trip", Toast.LENGTH_SHORT).show();
        }
        file = new File(file,
                System.currentTimeMillis() + ".jpeg");
        return file;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}