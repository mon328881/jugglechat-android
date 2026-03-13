package com.juggle.im.android.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.juggle.im.android.R;
import com.juggle.im.android.chat.zxing.ViewfinderView;

import java.io.IOException;

/**
 * 二维码扫描Activity
 */
public class ScanActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "ScanActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private SurfaceView surfaceView;
    private ViewfinderView viewfinderView;
    private Camera camera;
    private MultiFormatReader reader;
    private boolean isScanning = false;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        
        surfaceView = findViewById(R.id.preview_view);
        viewfinderView = findViewById(R.id.viewfinder_view);
        
        reader = new MultiFormatReader();
        
        // 检查摄像头权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            initCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera();
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initCamera() {
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isPaused) {
            return;
        }
        
        try {
            // 确保之前的摄像头已释放
            if (camera != null) {
                try {
                    camera.stopPreview();
                    camera.release();
                } catch (Exception e) {
                    Log.e(TAG, "Error releasing previous camera", e);
                }
                camera = null;
            }
            
            camera = Camera.open(0);
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback((data, camera) -> {
                if (!isScanning && !isPaused && camera != null) {
                    isScanning = true;
                    try {
                        decodeFrame(data, camera);
                    } finally {
                        isScanning = false;
                    }
                }
            });
            camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error opening camera", e);
            Toast.makeText(this, "无法打开摄像头", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error in surfaceCreated", e);
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null && !isPaused) {
            try {
                camera.stopPreview();
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewFormat(android.graphics.ImageFormat.NV21);
                
                // 设置摄像头旋转角度，使预览正确显示
                // 对于竖屏Activity，需要旋转90度
                camera.setDisplayOrientation(90);
                
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                
                // 设置摄像头预览的宽高，用于调整SurfaceView的宽高比
                Camera.Size previewSize = parameters.getPreviewSize();
                if (surfaceView instanceof com.juggle.im.android.chat.zxing.CameraSurfaceView) {
                    // 由于旋转了90度，需要交换宽高
                    ((com.juggle.im.android.chat.zxing.CameraSurfaceView) surfaceView)
                            .setCameraSize(previewSize.height, previewSize.width);
                }
                
                camera.startPreview();
            } catch (IOException e) {
                Log.e(TAG, "Error in surfaceChanged", e);
            } catch (Exception e) {
                Log.e(TAG, "Error in surfaceChanged", e);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing camera", e);
            }
            camera = null;
        }
    }

    private void decodeFrame(byte[] data, Camera camera) {
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            int width = size.width;
            int height = size.height;
            
            // 将NV21格式转换为RGB
            int[] rgb = decodeYUV420SP(data, width, height);
            
            // 解码二维码
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new com.google.zxing.RGBLuminanceSource(width, height, rgb)));
            Result result = reader.decodeWithState(binaryBitmap);
            
            if (result != null) {
                handleScanResult(result);
            }
        } catch (Exception e) {
            Log.d(TAG, "Decode error: " + e.getMessage());
        }
    }

    private int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {
        int frameSize = width * height;
        int[] rgb = new int[frameSize];
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) yuv420sp[i * width + j]));
                int u = (0xff & ((int) yuv420sp[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) yuv420sp[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;
                
                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));
                
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                
                rgb[i * width + j] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
        return rgb;
    }

    private void handleScanResult(Result result) {
        if (result != null) {
            String scanResult = result.getText();
            Log.d(TAG, "Scan result: " + scanResult);
            
            Intent intent = new Intent();
            intent.putExtra("scan_result", scanResult);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }
}
