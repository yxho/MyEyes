package com.yxh.myeyes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
//import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Scalar;
import org.opencv.core.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MyEyes";
    private Preview preview;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private PreviewView viewFinder;
    private ViewCanvas viewCanvas;
    private ImageView ivBitmap;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private float mRelativeFaceSize = 0.07f;
    private int mAbsoluteFaceSize = 0;

    private Rect[] facesArray = null;


    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private File outputDirectory;
    private ExecutorService cameraExecutor;
    private int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private ImageFormatConvert imageFormatConvert;
    private int currentImageType = Imgproc.COLOR_RGB2GRAY;
    private Mat mat;
    private Bitmap bitImage = null;
    private Matrix matrix = new Matrix();
    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载opencv");
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "成功加载opencv！", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    mat = new Mat();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    Toast toast1 = Toast.makeText(getApplicationContext(),
                            "加载失败！", Toast.LENGTH_LONG);
                    toast1.setGravity(Gravity.CENTER, 0, 0);
                    toast1.show();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button camera_capture_button = findViewById(R.id.camera_capture_button);
        viewFinder = findViewById(R.id.viewFinder);
        viewCanvas = findViewById(R.id.viewCanvas);
        cameraExecutor = Executors.newSingleThreadExecutor();
        ivBitmap = findViewById(R.id.ivBitmap);
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    MainActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        camera_capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
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
                            .build();
                    // 图片转换类初始化
                    imageFormatConvert = new ImageFormatConvert(MainActivity.this);

                    imageAnalyzer.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                        @Override
                        public void analyze(@NonNull ImageProxy image) {
                            imageAnalyzerFunc(image);
                            image.close();
                        }
                    });

                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    //重新绑定之前必须先取消绑定
                    cameraProvider.unbindAll();

                    camera = cameraProvider.bindToLifecycle(MainActivity.this,
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
                Toast.makeText(MainActivity.this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this, "Trip", Toast.LENGTH_SHORT).show();
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


    private Bitmap toBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

//        传统方法 比较慢
//        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 50, out);
//
//        byte[] imageBytes = out.toByteArray();
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return imageFormatConvert.nv21ToBitmap(nv21, image.getWidth(), image.getHeight());
    }

    private void imageAnalyzerFunc(ImageProxy image) {
        Date startDate = new Date();
        bitImage = toBitmap(image);
        // Log.e(TAG, "当前帧亮度:" + (ave / dst.length));
        // 设置旋转角度
        matrix.setRotate(90);
        // 调整图片大小，放大到PreviewView大小
       //matrix.postScale((float) viewFinder.getWidth() / bitImage.getHeight(), (float) viewFinder.getHeight() / bitImage.getWidth()); //此时bitImage处于旋转90度状态
        // 重新绘制Bitmap
        bitImage = Bitmap.createBitmap(bitImage, 0, 0, bitImage.getWidth(), bitImage.getHeight(), matrix, true);
        Utils.bitmapToMat(bitImage, mat);
        Imgproc.cvtColor(mat, mat, currentImageType);
        facesArray = onCameraFrame(mat);

        Date endDate = new Date();
        long diff = endDate.getTime() - startDate.getTime();
        Log.e(TAG, "处理时间差:" + diff);

        //Utils.matToBitmap(mat, bitImage);

//        viewCanvas.post(new Runnable() {
//            @Override
//            public void run() {
//                //ivBitmap.setImageBitmap(bitImage);
//                viewCanvas.setBitmap(bitImage);
//            }
//        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //ivBitmap.setImageBitmap(bitImage);
//                Matrix matrix = new Matrix();
//                matrix.postScale(0.5f, 0.5f);
//                canvas.drawBitmap(mBitmap, matrix,null);

                //viewCanvas.setBitmap(bitImage);
                viewCanvas.drawtarget(facesArray, (float) viewFinder.getWidth() / bitImage.getHeight(), (float) viewFinder.getHeight() / bitImage.getWidth());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Rect[] onCameraFrame(Mat mGray) {

        //mRgba = inputFrame.rgba();
        //mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 4, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());


//        Rect[] facesArray = faces.toArray();
//        for (int i = 0; i < facesArray.length; i++)
//            Imgproc.rectangle(mGray, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return faces.toArray();
    }
}