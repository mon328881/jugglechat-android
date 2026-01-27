package com.juggle.im.android.server.beans;

import com.google.gson.annotations.SerializedName;

/**
 * 二维码Bean
 */
public class QRCodeBean {
    @SerializedName("qr_code")
    private String qrCode;

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
