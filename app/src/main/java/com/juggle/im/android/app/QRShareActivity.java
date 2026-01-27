package com.juggle.im.android.app;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.juggle.im.android.R;
import com.juggle.im.android.model.ConfigUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 展示当前用户的二维码，供他人扫码添加好友。
 * 使用 APP 扫一扫可添加该用户；非 APP 扫码可跳转下载页。
 */
public class QRShareActivity extends AppCompatActivity {

    public static final String SCHEME_USER = "juggleim://user/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_share);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.qrcode_share_title);
        }

        String userId = getIntent().getStringExtra("user_id");
        String nickname = getIntent().getStringExtra("nickname");
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "无法获取用户信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvNickname = findViewById(R.id.tv_nickname);
        TextView tvUserId = findViewById(R.id.tv_user_id);
        ImageView ivQrcode = findViewById(R.id.iv_qrcode);
        TextView tvHintApp = findViewById(R.id.tv_hint_app);
        TextView tvHintWeb = findViewById(R.id.tv_hint_web);

        tvNickname.setText(TextUtils.isEmpty(nickname) ? userId : nickname);
        tvUserId.setText("@" + userId);

        // 二维码内容：优先使用 https 下载页（外部扫码可直接打开）；APP 内扫码可从参数中解析 user_id 并加好友
        String qrContent = buildQrContent(userId);
        Bitmap qrBitmap = generateQRCode(qrContent, 512);
        if (qrBitmap != null) {
            ivQrcode.setImageBitmap(qrBitmap);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private Bitmap generateQRCode(String content, int sizePx) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);

            int w = bitMatrix.getWidth();
            int h = bitMatrix.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }

    private String buildQrContent(String userId) {
        String base = ConfigUtils.appDownloadPageUrl;
        if (!TextUtils.isEmpty(base)) {
            try {
                Uri baseUri = Uri.parse(base);
                Uri.Builder builder = baseUri.buildUpon();
                builder.appendQueryParameter("user_id", userId);
                // 预留给 H5/浏览器端：若已安装 App，可用此 scheme 做一键唤起
                builder.appendQueryParameter("scheme", SCHEME_USER + userId);
                return builder.build().toString();
            } catch (Exception ignored) {
            }
        }
        return SCHEME_USER + userId;
    }
}
