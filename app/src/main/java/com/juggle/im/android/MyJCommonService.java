package com.juggle.im.android;

import cn.jpush.android.service.JCommonService;

/**
 * 极光推送所需的通用 Service
 * 必须在 AndroidManifest 中注册，否则 JPush SDK 会报 missing service。
 */
public class MyJCommonService extends JCommonService {
    // 留空即可，逻辑由 JPush SDK 处理
}

