package com.juggle.im.android.chat.zxing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * 扫描框视图
 */
public class ViewfinderView extends View {
    private static final int CORNER_WIDTH = 10;
    private static final int CORNER_LENGTH = 40;
    private static final int FRAME_WIDTH = 2;
    
    private Paint paint;
    private Rect frame;
    private int frameWidth;
    private int frameHeight;

    public ViewfinderView(Context context) {
        super(context);
        init();
    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // 计算扫描框大小
        frameWidth = (int) (width * 0.8);
        frameHeight = (int) (height * 0.6);
        
        int frameLeft = (width - frameWidth) / 2;
        int frameTop = (height - frameHeight) / 2;
        
        frame = new Rect(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight);
        
        // 绘制半透明遮罩
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        
        // 绘制扫描框边框
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(FRAME_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(frame, paint);
        
        // 绘制四个角
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(CORNER_WIDTH);
        
        // 左上角
        canvas.drawLine(frame.left, frame.top, frame.left + CORNER_LENGTH, frame.top, paint);
        canvas.drawLine(frame.left, frame.top, frame.left, frame.top + CORNER_LENGTH, paint);
        
        // 右上角
        canvas.drawLine(frame.right, frame.top, frame.right - CORNER_LENGTH, frame.top, paint);
        canvas.drawLine(frame.right, frame.top, frame.right, frame.top + CORNER_LENGTH, paint);
        
        // 左下角
        canvas.drawLine(frame.left, frame.bottom, frame.left + CORNER_LENGTH, frame.bottom, paint);
        canvas.drawLine(frame.left, frame.bottom, frame.left, frame.bottom - CORNER_LENGTH, paint);
        
        // 右下角
        canvas.drawLine(frame.right, frame.bottom, frame.right - CORNER_LENGTH, frame.bottom, paint);
        canvas.drawLine(frame.right, frame.bottom, frame.right, frame.bottom - CORNER_LENGTH, paint);
        
        // 绘制扫描线
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        int scanLineY = frame.top + (int) ((System.currentTimeMillis() / 10) % frameHeight);
        canvas.drawLine(frame.left, scanLineY, frame.right, scanLineY, paint);
        
        // 继续绘制
        postInvalidateDelayed(50);
    }

    public Rect getFramingRect() {
        return frame;
    }
}
