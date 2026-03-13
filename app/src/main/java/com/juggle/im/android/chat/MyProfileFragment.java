package com.juggle.im.android.chat;

import static com.juggle.im.android.app.LoginActivity.PREFS_NAME;
import static com.juggle.im.android.app.LoginActivity.KEY_APP_TOKEN;
import static com.juggle.im.android.app.LoginActivity.KEY_IM_TOKEN;
import static com.juggle.im.android.app.LoginActivity.KEY_EXPIRE_TIME;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.juggle.im.JIM;
import com.juggle.im.android.R;
import com.juggle.im.android.app.LoginActivity;
import com.juggle.im.android.model.ConfigUtils;
import com.juggle.im.android.chat.utils.FileUtils;
import com.juggle.im.android.server.beans.UserInfoBean;
import com.juggle.im.android.server.beans.UserInfoRequest;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.AvatarUtils;
import com.juggle.im.android.utils.UpdateChecker;

public class MyProfileFragment extends Fragment {

    private static final int REQ_EDIT_NICKNAME = 1001;
    private static final int REQ_PICK_AVATAR = 1002;
    
    private ImageView ivAvatar;
    private TextView tvNickname;
    private TextView tvUserId;
    private View llAvatarContainer;
    private View llNicknameContainer;
    private View llPermissionManage;
    private View llPrivacyPolicy;
    private View llVersionInfo;
    private View llCheckUpdate;
    private TextView tvVersionName;
    private View btnLogout;
    private ImageView ivQrcodeShare;

    private UserInfoBean currentUserInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        
        initViews(view);
        loadUserInfo();
        
        return view;
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvUserId = view.findViewById(R.id.tv_user_id);
        llAvatarContainer = view.findViewById(R.id.ll_avatar_container);
        llNicknameContainer = view.findViewById(R.id.ll_nickname_container);
        llPermissionManage = view.findViewById(R.id.ll_permission_manage);
        llPrivacyPolicy = view.findViewById(R.id.ll_privacy_policy);
        llVersionInfo = view.findViewById(R.id.ll_version_info);
        llCheckUpdate = view.findViewById(R.id.ll_check_update);
        tvVersionName = view.findViewById(R.id.tv_version_name);
        btnLogout = view.findViewById(R.id.btn_logout);
        ivQrcodeShare = view.findViewById(R.id.iv_qrcode_share);

        if (ivQrcodeShare != null) {
            ivQrcodeShare.setOnClickListener(v -> openQrcodeShare());
        }
        if (tvVersionName != null) {
            String vn = UpdateChecker.getLocalVersionName(getContext());
            tvVersionName.setText(vn != null ? vn : "--");
        }
        llAvatarContainer.setOnClickListener(v -> updateAvatar());
        llNicknameContainer.setOnClickListener(v -> updateNickname());
        llPermissionManage.setOnClickListener(v -> openPermissionManage());
        if (llPrivacyPolicy != null) {
            llPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        }
        if (llCheckUpdate != null) {
            llCheckUpdate.setOnClickListener(v -> {
                if (getActivity() == null) return;
                UpdateChecker.checkForUpdate(getActivity());
            });
        }
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadUserInfo() {
        // 优先用 SDK 当前用户 ID，SDK 未就绪时用登录时保存的 currentUserId，避免第二次登录后请求 /jim/users/info?user_id= 为空
        String userId = null;
        if (JIM.getInstance().getCurrentUserId() != null && !JIM.getInstance().getCurrentUserId().isEmpty()) {
            userId = JIM.getInstance().getCurrentUserId();
        } else if (ConfigUtils.currentUserId != null && !ConfigUtils.currentUserId.isEmpty()) {
            userId = ConfigUtils.currentUserId;
        }
        if (TextUtils.isEmpty(userId)) {
            tvUserId.setText("加载中…");
            return;
        }
        
        // 先显示本地缓存的用户信息，避免网络延迟导致界面空白
        showCachedUserInfo();
        
        ServiceManager.getUserService().getUserInfo(userId, new ApiCallback<UserInfoBean>() {
            @Override
            public void onSuccess(UserInfoBean data) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    currentUserInfo = data;
                    updateUI();
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    // 网络请求失败时，如果本地有缓存则不显示错误提示
                    if (currentUserInfo == null) {
                        Toast.makeText(getContext(), "获取用户信息失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showCachedUserInfo() {
        // 使用 ConfigUtils 中缓存的用户信息作为兜底
        if (!TextUtils.isEmpty(ConfigUtils.myName)) {
            tvNickname.setText(ConfigUtils.myName);
        }
        if (!TextUtils.isEmpty(ConfigUtils.myAvatarUrl)) {
            AvatarUtils.loadAvatar(ivAvatar, ConfigUtils.myAvatarUrl, ConfigUtils.myName);
        } else if (!TextUtils.isEmpty(ConfigUtils.myName)) {
            // 如果没有头像 URL，则根据昵称生成首字母头像
            AvatarUtils.loadAvatar(ivAvatar, null, ConfigUtils.myName);
        }
        if (!TextUtils.isEmpty(ConfigUtils.currentUserId)) {
            tvUserId.setText(ConfigUtils.currentUserId);
        }
    }

    private void updateUI() {
        if (currentUserInfo == null) return;

        // 加载头像：与聊天列表保持一致，优先使用头像 URL，缺失时按昵称/ID 生成首字母头像
        String avatarUrl = currentUserInfo.getAvatar();
        String name = !TextUtils.isEmpty(currentUserInfo.getNickname())
                ? currentUserInfo.getNickname()
                : currentUserInfo.getUserId();
        AvatarUtils.loadAvatar(ivAvatar, avatarUrl, name);

        // 显示昵称
        if (!TextUtils.isEmpty(currentUserInfo.getNickname())) {
            tvNickname.setText(currentUserInfo.getNickname());
        } else {
            tvNickname.setText("未设置");
        }

        // 显示用户ID
        tvUserId.setText(currentUserInfo.getUserId());
    }

    private void updateAvatar() {
        Context ctx = getContext();
        if (ctx == null) return;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        try {
            startActivityForResult(intent, REQ_PICK_AVATAR);
        } catch (Exception e) {
            Toast.makeText(ctx, "无法打开相册，请检查系统相册/文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPermissionManage() {
        Intent intent = new Intent(getActivity(), PermissionManageActivity.class);
        startActivity(intent);
    }

    private void openPrivacyPolicy() {
        Context ctx = getActivity();
        if (ctx == null) return;
        Intent intent = new Intent(ctx, com.juggle.im.android.app.PrivacyPolicyActivity.class);
        startActivity(intent);
    }

    private void openQrcodeShare() {
        if (getActivity() == null) return;
        String userId = JIM.getInstance().getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            userId = ConfigUtils.currentUserId;
        }
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "无法获取用户信息", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), com.juggle.im.android.app.QRShareActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("nickname", currentUserInfo != null ? currentUserInfo.getNickname() : "");
        startActivity(intent);
    }

    private void updateNickname() {
        // 优先使用当前用户信息中的昵称，其次使用界面上已显示的昵称
        String currentName = null;
        if (currentUserInfo != null && !TextUtils.isEmpty(currentUserInfo.getNickname())) {
            currentName = currentUserInfo.getNickname();
        } else if (tvNickname != null) {
            CharSequence text = tvNickname.getText();
            currentName = text != null ? text.toString() : null;
        }

        // 跳转到编辑昵称页面
        Intent intent = new Intent(getActivity(), EditNicknameActivity.class);
        intent.putExtra("current_nickname", currentName);
        startActivityForResult(intent, REQ_EDIT_NICKNAME);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_EDIT_NICKNAME && resultCode == getActivity().RESULT_OK && data != null) {
            String newNickname = data.getStringExtra("new_nickname");
            if (!TextUtils.isEmpty(newNickname)) {
                updateUserInfo(newNickname, null);
            }
        } else if (requestCode == REQ_PICK_AVATAR && resultCode == getActivity().RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) {
                Toast.makeText(getContext(), "未获取到图片", Toast.LENGTH_SHORT).show();
                return;
            }
            // 先本地预览，提升反馈速度
            try {
                Glide.with(this).load(uri).circleCrop().into(ivAvatar);
            } catch (Throwable ignored) {
            }

            Context ctx = getContext();
            if (ctx == null) return;
            String ext = guessImageExt(ctx, uri);
            String suffix = "temp_avatar." + ext;
            String localPath = FileUtils.convertContentUriToFile(ctx.getApplicationContext(), uri.toString(), suffix);
            if (TextUtils.isEmpty(localPath)) {
                Toast.makeText(getContext(), "无法读取图片文件", Toast.LENGTH_SHORT).show();
                updateUI();
                return;
            }

            Toast.makeText(getContext(), "正在上传头像…", Toast.LENGTH_SHORT).show();
            // FileType: 1 图片
            ServiceManager.getFileService().uploadFile(1, localPath, ext, new ApiCallback<String>() {
                @Override
                public void onSuccess(String url) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> updateUserInfo(null, url));
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "头像上传失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                        updateUI(); // 回退到原头像
                    });
                }
            });
        }
    }

    private static String guessImageExt(Context ctx, Uri uri) {
        try {
            String mime = ctx.getContentResolver().getType(uri);
            if ("image/png".equalsIgnoreCase(mime)) return "png";
            if ("image/webp".equalsIgnoreCase(mime)) return "webp";
            if ("image/heic".equalsIgnoreCase(mime) || "image/heif".equalsIgnoreCase(mime)) return "heic";
            if ("image/jpeg".equalsIgnoreCase(mime) || "image/jpg".equalsIgnoreCase(mime)) return "jpg";
        } catch (Exception ignored) {
        }
        return "jpg";
    }

    private void updateUserInfo(String nickname, String avatar) {
        // 为了避免只改昵称时服务端把头像清空，这里始终把当前的昵称和头像一并提交
        String userId = JIM.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(getContext(), "当前用户未登录，无法更新资料", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalNickname = nickname;
        String finalAvatar = avatar;
        if (currentUserInfo != null) {
            if (finalNickname == null) {
                finalNickname = currentUserInfo.getNickname();
            }
            if (finalAvatar == null) {
                finalAvatar = currentUserInfo.getAvatar();
            }
        }

        final String nicknameToSave = finalNickname;
        final String avatarToSave = finalAvatar;

        UserInfoRequest request = new UserInfoRequest();
        request.setUserId(userId);
        if (!TextUtils.isEmpty(nicknameToSave)) {
            request.setNickname(nicknameToSave);
        }
        if (!TextUtils.isEmpty(avatarToSave)) {
            request.setAvatar(avatarToSave);
        }

        ServiceManager.getUserService().updateUserInfo(request, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "更新成功", Toast.LENGTH_SHORT).show();
                    // 更新本地缓存
                    if (!TextUtils.isEmpty(nicknameToSave)) {
                        ConfigUtils.myName = nicknameToSave;
                        if (currentUserInfo != null) {
                            currentUserInfo.setNickname(nicknameToSave);
                        }
                    }
                    if (!TextUtils.isEmpty(avatarToSave)) {
                        ConfigUtils.myAvatarUrl = avatarToSave;
                        if (currentUserInfo != null) {
                            currentUserInfo.setAvatar(avatarToSave);
                        }
                    }
                    updateUI();
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "更新失败: " + errorMsg, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void logout() {
        // 清除用户信息
        ConfigUtils.appToken = null;
        ConfigUtils.imToken = null;
        ConfigUtils.myName = null;
        ConfigUtils.myAvatarUrl = null;
        ConfigUtils.currentUserId = null;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // 仅清除登录态相关字段，保留"记住账号"配置，方便下次登录自动填充账号
        editor.remove(KEY_APP_TOKEN);
        editor.remove(KEY_IM_TOKEN);
        editor.remove(KEY_EXPIRE_TIME);
        editor.apply();
        JIM.getInstance().getConnectionManager().disconnect(false);

        // 跳转到登录页面
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}