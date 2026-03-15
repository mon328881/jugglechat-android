# JuggleChat Android 性能优化与安全风险整改方案

本文档基于对 `jugglechat-android` 的代码审查，给出**性能优化**、**内存泄漏风险**和**安全风险**的整改方案与优先级。

---

## 一、内存泄漏与生命周期风险

### 1.1 高优先级：Handler / 延迟任务未在销毁时清理

| 位置 | 问题 | 建议 |
|------|------|------|
| **FlashActivity** | `new Handler().postDelayed(this::goToLogin, 1500)` / `goToMain`：匿名 Handler 持有 Activity，若用户在 1.5s 内旋转或退出，回调仍会执行并可能操作已销毁的 Activity | 使用 `Handler(Looper.getMainLooper())` 并保存为成员变量，在 `onDestroy()` 中 `handler.removeCallbacksAndMessages(null)` |
| **UpdateChecker** | 静态 `Handler MAIN` 与 `OkHttpClient`，异步回调里通过 `MAIN.post(...)` 使用传入的 `Activity`；若请求慢，Activity 可能已销毁 | 回调中已有 `isActivityUsable(activity)` 校验，建议在 `checkForUpdate` 入参改用 `WeakReference<Activity>`，并在所有使用 activity 的地方先 `get()` 判空 |
| **AvatarUtils.loadVideoCover** | 静态 `mainHandler` + `videoCoverExecutor`，后台取帧后 post 到主线程更新 ImageView，未检查 Fragment/Activity 是否已 destroy | 在 `mainHandler.post` 的 Runnable 内用 `View.getContext()` 判断是否仍为 Activity 且未 isFinishing，或让调用方传入 Lifecycle 并在非活跃时取消 |
| **VoiceInputAction** | 实例 `uiHandler` 与 `postDelayed` | 在 View 的 `onDetachedFromWindow` 或 Fragment 的 `onDestroyView` 中 `uiHandler.removeCallbacksAndMessages(null)` |
| **ChatInputActionBar** | `activityResultHandlers` Map 持有插件/回调 | 在 Activity/Fragment 的 `onDestroy` 中清空 `activityResultHandlers`，避免间接持有 Activity |

### 1.2 中优先级：Fragment 中 getActivity() 使用

| 位置 | 问题 | 建议 |
|------|------|------|
| **MyProfileFragment** | 多处 `getActivity().runOnUiThread(...)`：在 `onSuccess`/`onError` 里先判了 `getActivity()==null` 再 runOnUiThread，理论上安全；但 127、137、285、291、371、411 行等若在 runOnUiThread 执行时 Fragment 已 detach，getContext()/getActivity() 可能为 null | 在 runOnUiThread 的 lambda 内部再次判空 `if (getActivity() == null) return;`，或使用 `viewLifecycleOwner.lifecycleScope.launchWhenStarted { }` 替代，避免在已销毁后更新 UI |
| **ConversationListFragment / ForwardConversationListFragment** | 已有 `if (getActivity() != null)` 再 runOnUiThread，相对安全 | 保持习惯，其它 Fragment 可统一采用相同模式 |

### 1.3 建议：引入 LeakCanary 与 StrictMode

- **LeakCanary**：在 debug 依赖中增加 `debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'`，不写代码即可在开发时发现 Activity/Fragment 泄漏。
- **StrictMode**：在 `Application.onCreate()` 的 debug 分支中启用 `StrictMode.setThreadPolicy` / `setVmPolicy`，检测主线程磁盘/网络访问与泄漏的 Closeable。

---

## 二、性能优化

### 2.1 主线程与 IO/网络

| 位置 | 问题 | 建议 |
|------|------|------|
| **ConversationListFragment** | `new Thread(() -> { ... syncConversationList ... }).start()` 内通过 `getActivity().runOnUiThread` 更新 UI | 确保 `getActivity()` 判空；可考虑改为 Kotlin 协程或 Executor + 主线程 Handler，便于取消 |
| **BaseService** | 已在子线程做网络请求，结果通过 `mainHandler.post` 回主线程 | 保持；确保回调中不持有 Activity 强引用 |
| **CreatePostActivity** | `ThumbnailUtils.createVideoThumbnail` 及 Bitmap 处理 | 确认不在主线程执行；若在 UI 线程，请移到 `ExecutorService` 或 `AsyncTask` 替代方案（如 Executor） |
| **MyProfileFragment** | `BitmapFactory.decodeStream` 与 `bitmap.compress` 处理头像 | 确认在后台线程执行；若在 `onActivityResult` 中直接调用，请移到子线程 |

### 2.2 Bitmap 与图片

| 位置 | 问题 | 建议 |
|------|------|------|
| **FileMessageView** | `ThumbnailUtils.createVideoThumbnail` 得到 Bitmap 后 setImageBitmap，未 recycle；在 RecyclerView 中频繁 bind 可能产生多份 Bitmap | 若不再使用旧 Bitmap，在设置新 Bitmap 前对旧 Bitmap 做 `recycle()`（需判断 isRecycled）；或改用 Glide 加载视频封面，统一生命周期与缓存 |
| **ImageCompressionUtils** | 已对 scaledBitmap/bitmap 做 recycle | 保持 |
| **AvatarUtils.createInitialsBitmap** | 创建的 Bitmap 交给 ImageView 显示 | ImageView 回收时无法自动 recycle；若同一 View 会多次生成新的 initials Bitmap，建议对上一次的 Bitmap 做 recycle 或使用对象池 |
| **大图解码** | 任何 `BitmapFactory.decodeFile/decodeStream` 未指定 `inSampleSize` | 对大图使用 `BitmapFactory.Options.inSampleSize` 做缩放到目标尺寸，避免 OOM |

### 2.3 列表与滑动

- **RecyclerView**：所有 Adapter 已用 `RecyclerView.Adapter`，未发现 ListView。建议：
  - `onBindViewHolder` 中只做轻量赋值，耗时操作（如网络、大图解码）放后台或交给 Glide。
  - 对长列表考虑 `RecyclerView.setItemViewCacheSize`、`setRecycledViewPool` 调优（按需）。
- **Glide**：已大量使用，且 `Glide.with(activity/fragment)` 能绑生命周期。建议统一通过 Glide 加载网络/本地图与视频封面，避免手写 Bitmap 未 recycle 的泄漏。

---

## 三、安全风险

### 3.1 高优先级：敏感配置与存储

| 项目 | 问题 | 建议 |
|------|------|------|
| **ConfigUtils** | `appKey`、`appServerUrl`、`imServer`、`zegoId` 等硬编码在代码中；若仓库公开，密钥会泄露 | 移至 `local.properties` 或 `BuildConfig`（不提交仓库），通过 `build.gradle` 读取并注入 `BuildConfig`；或使用 NDK 存密钥（仅提高门槛） |
| **build.gradle** | `JPUSH_APPKEY`、`storePassword`、`keyPassword` 等写在脚本中 | 签名密码与第三方 key 放入 `local.properties` 或环境变量，由 CI/本地读取，不提交 |
| **SharedPreferences** | Token、账号等存于 `MODE_PRIVATE` 的 SharedPreferences，未加密；root 或备份可读 | 对 token 等敏感项使用 EncryptedSharedPreferences（AndroidX Security 库）；或 AES 加密后写入普通 prefs |
| **ConfigUtils 静态 token** | `appToken`、`imToken`、`currentUserId` 等为静态变量，进程内全局可访问 | 保持从 EncryptedSharedPreferences 恢复；避免在日志或异常信息中打印 token |

### 3.2 中优先级：网络与依赖

| 项目 | 问题 | 建议 |
|------|------|------|
| **HTTPS** | `appServerUrl`、`imServer` 使用 `http://`、`ws://` | 生产环境改为 `https://`、`wss://`，并配置证书校验（避免自定义 TrustManager 放行所有证书） |
| **依赖与 ProGuard** | release 已使用 proguard；需确认第三方 SDK（如极光、即构、腾讯地图）的混淆规则 | 按各 SDK 文档配置 keep 规则，避免混淆导致崩溃或接口不可用 |

### 3.3 低优先级：其它

- **日志**：避免在 release 中打印 token、userId、完整 URL；可统一通过 `BuildConfig.DEBUG` 控制。
- **WebView**：当前未使用；若后续引入，需固定 `setJavaScriptEnabled`、校验 URL、避免 `addJavascriptInterface` 暴露敏感 API。

---

## 四、实施优先级建议

| 优先级 | 类型 | 项 |
|--------|------|----|
| P0 | 安全 | Token/密钥移出代码与 build.gradle；SharedPreferences 敏感项改为 EncryptedSharedPreferences |
| P0 | 泄漏 | FlashActivity Handler 在 onDestroy 移除回调；LeakCanary 接入 |
| P1 | 泄漏 | UpdateChecker 使用 WeakReference&lt;Activity&gt;；AvatarUtils.loadVideoCover 生命周期/判空；VoiceInputAction、ChatInputActionBar 在销毁时清理 Handler/Map |
| P1 | 性能 | 确保 CreatePostActivity、MyProfileFragment 中 Bitmap/Thumbnail 在子线程；FileMessageView 考虑 Glide 或 Bitmap.recycle |
| P2 | 性能 | StrictMode 在 debug 开启；RecyclerView/Glide 按需调优 |
| P2 | 安全 | 生产环境 HTTPS/WSS；日志脱敏 |

---

## 五、快速修改示例

### 5.1 FlashActivity：Handler 清理

```java
// 成员变量
private final Handler handler = new Handler(Looper.getMainLooper());
private final Runnable goToLoginRunnable = this::goToLogin;
private final Runnable goToMainRunnable = this::goToMain;

@Override
protected void onCreate(Bundle savedInstanceState) {
    // ...
    if (hasValidToken()) {
        handler.postDelayed(goToMainRunnable, 1200);
    } else {
        handler.postDelayed(goToLoginRunnable, 1500);
    }
}

@Override
protected void onDestroy() {
    handler.removeCallbacksAndMessages(null);
    super.onDestroy();
}
```

### 5.2 build.gradle：LeakCanary 与 StrictMode 准备

```gradle
dependencies {
    // ...
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
}
```

在 `Application.onCreate()` 中（仅 debug）：

```java
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectLeakedClosableObjects().detectLeakedSqlLiteObjects().penaltyLog().build());
}
```

### 5.3 敏感配置迁移（BuildConfig）

在 `app/build.gradle` 中：

```gradle
def localProperties = new Properties()
def localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(new FileInputStream(localFile))
}

android {
    defaultConfig {
        buildConfigField "String", "APP_KEY", "\"${localProperties.getProperty("APP_KEY", "")}\""
        buildConfigField "String", "APP_SERVER_URL", "\"${localProperties.getProperty("APP_SERVER_URL", "")}\""
        buildConfigField "String", "IM_SERVER", "\"${localProperties.getProperty("IM_SERVER", "")}\""
    }
}
```

在 `local.properties`（不提交到 git）中配置：

```properties
APP_KEY=your_key
APP_SERVER_URL=http://192.168.123.214:8070
IM_SERVER=ws://192.168.123.214:9003
```

代码中改为使用 `BuildConfig.APP_KEY` 等。

---

以上方案可直接按优先级分阶段落地；先做 P0 的安全与泄漏项，再逐步完成 P1/P2，并配合 LeakCanary 与真机测试验证。

---

## 六、P2 实施说明（已完成）

- **日志脱敏**：新增 `utils/LogUtil.java`，仅 `BuildConfig.DEBUG` 时输出 d/i/w/e；Application、LoginActivity、MainActivity、GroupListActivity 等敏感处改为使用 `LogUtil`，不再打印 token、registrationId、userId、账号、完整 URL。
- **生产环境 HTTPS/WSS**：`Application.onCreate()` 在 Debug 下若检测到 `appServerUrl` 非 https 或 `imServer` 非 wss，会打一条 LogUtil 提示；`local.properties.example` 中已注明生产环境请配置 https/wss。
- **RecyclerView 调优**：`MessageListFragment`、`ConversationListFragment`、`MomentsActivity` 的 RecyclerView 已设置 `setItemViewCacheSize(20/24)`，减少滑动时重复创建 ViewHolder。
