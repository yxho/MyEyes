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

public class ViewCanvas extends View {
    private Paint paint;
    private Bitmap bitImage = null;

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
        if (bitImage != null) {
            canvas.drawBitmap(bitImage, 0, 0, null);
        }

    }

    public void drawCircle(Canvas canvas) {
        paint.setColor(0xff8bc5ba);//设置颜色
        paint.setStyle(Paint.Style.STROKE);//绘图为线条模式
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int halfCanvasWidth = canvasWidth / 2;
        int count = 3;
        int D = canvasHeight / (count + 1);
        int R = D / 2;
        float strokeWidth = (float) (R * 0.25);

        //绘制圆
        //canvas.translate(0, D / (count + 1));
        ;//把当前画布的原点移到(10,10),后面的操作都以(10,10)作为参照点，默认原点为(0,0)
        canvas.drawCircle(halfCanvasWidth, R, R, paint);

    }

    /**
     *
     * @param image
     */
    public void setBitmap(Bitmap image) {
        bitImage = image;
        invalidate();
    }
}
