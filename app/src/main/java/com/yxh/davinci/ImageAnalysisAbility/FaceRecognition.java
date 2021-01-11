package com.yxh.davinci.ImageAnalysisAbility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.yxh.davinci.Utils.ImageFormatConvert;
import com.yxh.davinci.R;
import com.yxh.davinci.ViewCanvas;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaceRecognition {
    private String TAG = "MyEyes_FaceRecognition_opencv";
    private ImageFormatConvert imageFormatConvert;
    private int currentImageType = Imgproc.COLOR_RGB2GRAY;
    private Mat mat;
    private Bitmap bitImage = null;
    private Matrix matrix = new Matrix();
    private CascadeClassifier mJavaDetector;
    private File mCascadeFile;
    private float mRelativeFaceSize = 0.07f;
    private int mAbsoluteFaceSize = 0;
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    public FaceRecognition(Context context){
        imageFormatConvert = new ImageFormatConvert(context);
        //OpenCV库加载并初始化成功后的回调函数
        BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                // TODO Auto-generated method stub
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        Log.i(TAG, "成功加载opencv");
                        Toast toast = Toast.makeText(context.getApplicationContext(),
                                "成功加载opencv！", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        try {
                            // load cascade file from application resources
                            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
                            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
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
                        Toast toast1 = Toast.makeText(context.getApplicationContext(),
                                "加载失败！", Toast.LENGTH_LONG);
                        toast1.setGravity(Gravity.CENTER, 0, 0);
                        toast1.show();
                        break;
                }

            }
        };

        // 加载OpenCV library
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

            mat = new Mat();
        }

    }
    public void FindFacePosition(ImageProxy image, ViewCanvas viewCanvas, PreviewView viewFinder) {
        bitImage = imageFormatConvert.imageProxyToBitmap(image);
        // Log.e(TAG, "当前帧亮度:" + (ave / dst.length));
        // 设置旋转角度
        matrix.setRotate(90);
        // 调整图片大小，放大到PreviewView大小
        //matrix.postScale((float) viewFinder.getWidth() / bitImage.getHeight(), (float) viewFinder.getHeight() / bitImage.getWidth()); //此时bitImage处于旋转90度状态
        // 重新绘制Bitmap
        bitImage = Bitmap.createBitmap(bitImage, 0, 0, bitImage.getWidth(), bitImage.getHeight(), matrix, true);
        Utils.bitmapToMat(bitImage, mat);
        Imgproc.cvtColor(mat, mat, currentImageType);

        if (mAbsoluteFaceSize == 0) {
            int height = mat.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mat, faces, 1.1, 4, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        Utils.matToBitmap(mat, bitImage);
        viewCanvas.post(new Runnable() {
            @Override
            public void run() {
                //viewCanvas.setBitmap(bitImage);
                viewCanvas.drawtarget(faces.toArray(), (float) viewFinder.getWidth() / bitImage.getWidth(), (float) viewFinder.getHeight() / bitImage.getHeight());
            }
        });


    }
}

