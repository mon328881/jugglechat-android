package com.juggle.im.android;
import androidx.multidex.MultiDexApplication;

import com.juggle.im.android.core.JIMChatCore;
import com.juggle.im.android.model.ConfigUtils;
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer;

import java.util.Collections;

public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        
<<<<<<< Updated upstream
        // 腾讯地图隐私协议同意（必须在地图初始化之前调用）
        try {
            TencentMapInitializer.setAgreePrivacy(true);
        } catch (Throwable ignored) {
            // 如果地图SDK类不可用，不要崩溃应用启动
        }
        
=======
>>>>>>> Stashed changes
        // 主题通过 AndroidManifest.xml 中的 android:theme 属性应用
        JIMChatCore.getInstance().init(this, Collections.singletonList(ConfigUtils.imServer), ConfigUtils.appKey);
    }
}
