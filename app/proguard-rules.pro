# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ========== 混淆与防红防毒：保留堆栈行号便于排查，但隐藏源文件名 ==========
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========== 本应用：Gson 序列化/反序列化的数据类（必须保留字段名） ==========
-keep class com.juggle.im.android.server.beans.** { *; }

# ========== Gson ==========
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.Unsafe
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ========== OkHttp & Retrofit ==========
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ========== Glide ==========
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { *; }
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder { *; }
-keep class * implements com.bumptech.glide.load.model.GlideUrl { *; }
-keep class * implements com.bumptech.glide.load.model.StreamEncoder { *; }
-keep class * implements com.bumptech.glide.load.Encoder { *; }
-dontwarn com.bumptech.glide.**

# ========== Hilt / Dagger ==========
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.hilt.**

# ========== EventBus ==========
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep class org.greenrobot.eventbus.** { *; }

# ========== Protobuf ==========
-keep class * extends com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# ========== JuggleIM SDK ==========
-keep class com.juggle.im.** { *; }
-dontwarn com.juggle.im.**

# ========== Java-WebSocket ==========
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**

# ========== 极光推送 JPush ==========
-keep class cn.jiguang.** { *; }
-dontwarn cn.jiguang.**

# ========== 即构 Zego ==========
-keep class im.zego.** { *; }
-keep class com.juggle.call.zego.** { *; }
-dontwarn im.zego.**
-dontwarn com.juggle.call.zego.**

# ========== ExoPlayer ==========
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# ========== 七牛 ==========
-keep class com.qiniu.** { *; }
-dontwarn com.qiniu.**

# ========== Tencent Map SDK ==========
-keep public class com.tencent.lbssearch.** { *; }
-keep public class com.tencent.map.** { *; }
-keep public class com.tencent.mapsdk.** { *; }
-keep public class com.tencent.tencentmap.** { *; }
-keep public class com.tencent.tmsbeacon.** { *; }
-dontwarn com.qq.**
-dontwarn com.tencent.**

# ========== Android 组件与反射 ==========
-keep class * extends android.app.Application { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.app.Service { *; }
-keepclassmembers class * {
    native <methods>;
}
-keepclassmembers enum * { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ========== Conscrypt / 平台 SSL ==========
-dontwarn org.conscrypt.**
-dontwarn com.android.org.conscrypt.**
-dontwarn org.apache.harmony.xnet.provider.jsse.**