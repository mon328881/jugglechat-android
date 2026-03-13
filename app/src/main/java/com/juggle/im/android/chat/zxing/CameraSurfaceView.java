package com.juggle.im.android.chat.zxing;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * 自定义SurfaceView，用于正确显示摄像头预览
 * 根据摄像头预览的宽高比调整SurfaceView的大小
 */
public class CameraSurfaceView extends SurfaceView {
    private int cameraWidth = 0;
    private int cameraHeight = 0;

    public CameraSurfaceView(Context context) {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置摄像头预览的宽高
     */
    public void setCameraSize(int width, int height) {
        this.cameraWidth = width;
        this.cameraHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (cameraWidth > 0 && cameraHeight > 0) {
            // 计算摄像头的宽高比
            float cameraRatio = (float) cameraWidth / cameraHeight;
            // 计算屏幕的宽高比
            float screenRatio = (float) width / height;

            if (cameraRatio > screenRatio) {
                // 摄像头宽度相对较大，按高度调整
                height = (int) (width / cameraRatio);
            } else {
                // 摄像头高度相对较大，按宽度调整
                width = (int) (height * cameraRatio);
            }
        }

        setMeasuredDimension(width, height);
    }
}
