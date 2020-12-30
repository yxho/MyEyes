package com.yxh.myeyes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class ViewCanvas extends View {
    private Paint paint;
    private Bitmap bitImage = null;
    private int targetCounts = 0;
    private Rect[] mFacesArray;
    private float scaleX = 1;
    private float scaleY = 1;

    public ViewCanvas(Context context) {
        super(context);
        paint = new Paint();
    }

    public ViewCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    public ViewCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
    }

    public ViewCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
//        if (bitImage != null) {
//            canvas.drawBitmap(bitImage, 0, 0, null);
//        }

    }

    public void drawCircle(Canvas canvas) {
        paint.setColor(0xff8bc5ba);//设置颜色
        paint.setStyle(Paint.Style.FILL);//绘图为线条模式
//        int canvasWidth = canvas.getWidth();
//        int canvasHeight = canvas.getHeight();
//        int halfCanvasWidth = canvasWidth / 2;
//        int count = 3;
//        int D = canvasHeight / (count + 1);
//        int R = D / 2;
//        float strokeWidth = (float) (R * 0.25);

        //绘制圆
        //canvas.translate(0, D / (count + 1));
        ;//把当前画布的原点移到(10,10),后面的操作都以(10,10)作为参照点，默认原点为(0,0)

        for (int i = 0; i < targetCounts; i++) {
            // 图片被裁减过，所以 +50 ， -200
            int x = (int) ((mFacesArray[i].tl().x + mFacesArray[i].br().x+ 100) * 0.5f * scaleX) ;
            int y = (int) ((mFacesArray[i].br().y + mFacesArray[i].tl().y- 100) * 0.5f * scaleY) ;
            int R = (int) ((mFacesArray[i].br().x - mFacesArray[i].tl().x) * scaleX + (mFacesArray[i].br().y - mFacesArray[i].tl().y) * scaleY) / 3;

            canvas.drawCircle(x, y, R, paint);
        }
//        canvas.drawCircle(halfCanvasWidth, R, R, paint);

    }

    /**
     * @param image
     */
    public void setBitmap(Bitmap image) {
        bitImage = image;
        invalidate();
    }

    /**
     * @param
     */
    public void drawtarget(Rect[] facesArray, float sx, float sy) {
        targetCounts = facesArray.length;
        mFacesArray = facesArray;
        scaleX = sx;
        scaleY = sy;

        invalidate();
    }
}
