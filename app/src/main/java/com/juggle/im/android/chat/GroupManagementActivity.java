package com.juggle.im.android.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.juggle.im.JIM;
import com.juggle.im.android.R;
import com.juggle.im.android.event.ConversationIdDeletedEvent;
import com.juggle.im.android.server.beans.GroupDetailBean;
import com.juggle.im.android.server.beans.GroupManagementBean;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.HiddenConversationStore;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组管理设置页面
 * 用于管理群组权限、管理员等设置
 */
public class GroupManagementActivity extends AppCompatActivity {
    private static final String TAG = "GroupManagementActivity";
    private static final String EXTRA_GROUP_ID = "group_id";
    
    private String groupId;
    private int myRole = GroupRole.MEMBER;
    private GroupManagementBean groupManagement;
    
    // UI元素
    private TextView tvAddMemberPermission;
    private TextView tvPinMessagePermission;
    private TextView tvMentionAllPermission;
    private TextView tvEditGroupInfoPermission;
    private TextView tvSendMessagePermission;
    private SwitchCompat switchHistoryMessage;

    public static Intent intentFor(Context ctx, String groupId) {
        Intent i = new Intent(ctx, GroupManagementActivity.class);
        i.putExtra(EXTRA_GROUP_ID, groupId);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        
        initViews();
        loadGroupInfo();
    }
    
    private void initViews() {
        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        
        // 获取TextView引用
        tvAddMemberPermission = findViewById(R.id.tv_add_member_permission);
        tvPinMessagePermission = findViewById(R.id.tv_pin_message_permission);
        tvMentionAllPermission = findViewById(R.id.tv_mention_all_permission);
        tvEditGroupInfoPermission = findViewById(R.id.tv_edit_group_info_permission);
        tvSendMessagePermission = findViewById(R.id.tv_send_message_permission);
        switchHistoryMessage = findViewById(R.id.switch_history_message);

        // 谁可以添加成员
        findViewById(R.id.ll_add_member_permission).setOnClickListener(v -> {
            if (!checkOwnerPermission()) return;
            showPermissionDialog("添加成员权限", 
                groupManagement.getGroupAddMemberRight(),
                GroupPermission.KEY_ADD_MEMBER);
        });

        // 谁可以置顶消息
        findViewById(R.id.ll_pin_message_permission).setOnClickListener(v -> {
            if (!checkOwnerPermission()) return;
            showPermissionDialog("置顶消息权限",
                groupManagement.getGroupTopMsgRight(),
                GroupPermission.KEY_TOP_MSG);
        });

        // 谁可以 @ 所有人
        findViewById(R.id.ll_mention_all_permission).setOnClickListener(v -> {
            if (!checkOwnerPermission()) return;
            showPermissionDialog("@所有人权限",
                groupManagement.getGroupMentionAllRight(),
                GroupPermission.KEY_MENTION_ALL);
        });

        // 谁可以编辑群信息
        findViewById(R.id.ll_edit_group_info_permission).setOnClickListener(v -> {
            if (!checkOwnerPermission()) return;
            showPermissionDialog("编辑群信息权限",
                groupManagement.getGroupEditMsgRight(),
                GroupPermission.KEY_EDIT_MSG);
        });

        // 谁可以在群里发言
        findViewById(R.id.ll_send_message_permission).setOnClickListener(v -> {
            if (!checkOwnerPermission()) return;
            showPermissionDialog("发言权限",
                groupManagement.getGroupSendMsgRight(),
                GroupPermission.KEY_SEND_MSG);
        });

        // 新人进群获取历史消息开关
        switchHistoryMessage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // 忽略程序设置
            if (!checkOwnerPermission()) {
                buttonView.setChecked(!isChecked); // 恢复原状态
                return;
            }
            updateHistoryMessageSetting(isChecked);
        });

        // 群管理员
        findViewById(R.id.ll_group_admins).setOnClickListener(v -> {
            if (!checkOwnerPermission()) return;
            startActivity(GroupAdminsActivity.intentFor(this, groupId));
        });

        // 转让群主
        findViewById(R.id.ll_transfer_owner).setOnClickListener(v -> {
            if (!checkOwnerPermission()) return;
            startActivity(TransferOwnerActivity.intentFor(this, groupId));
        });

        // 解散群组
        findViewById(R.id.btn_dismiss_group).setOnClickListener(v -> {
            if (!checkOwnerPermission()) return;
            showDismissGroupDialog();
        });
    }
    
    /**
     * 加载群组信息
     */
    private void loadGroupInfo() {
        ServiceManager.getUserService().getGroupInfo(groupId, new ApiCallback<GroupDetailBean>() {
            @Override
            public void onSuccess(GroupDetailBean data) {
                if (data == null) return;
                
                myRole = data.getMyRole();
                groupManagement = data.getGroupManagement();
                
                if (groupManagement == null) {
                    groupManagement = new GroupManagementBean();
                }
                
                updateUI();
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "加载群组信息失败: " + code + " " + message);
                Toast.makeText(GroupManagementActivity.this, 
                    "加载群组信息失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 更新UI显示
     */
    private void updateUI() {
        if (groupManagement == null) return;
        
        // 更新权限显示
        tvAddMemberPermission.setText(
            GroupPermission.getPermissionText(groupManagement.getGroupAddMemberRight()));
        tvPinMessagePermission.setText(
            GroupPermission.getPermissionText(groupManagement.getGroupTopMsgRight()));
        tvMentionAllPermission.setText(
            GroupPermission.getPermissionText(groupManagement.getGroupMentionAllRight()));
        tvEditGroupInfoPermission.setText(
            GroupPermission.getPermissionText(groupManagement.getGroupEditMsgRight()));
        tvSendMessagePermission.setText(
            GroupPermission.getPermissionText(groupManagement.getGroupSendMsgRight()));
        
        // 更新历史消息开关（注意：后端存储的是hide_grp_msg，需要反转）
        switchHistoryMessage.setChecked(groupManagement.getHistoryMessageVisible() == 1);
    }
    
    /**
     * 检查是否有群主权限
     */
    private boolean checkOwnerPermission() {
        if (!GroupRole.isOwner(myRole)) {
            Toast.makeText(this, "只有群主可以修改此设置", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    
    /**
     * 显示权限选择对话框
     */
    private void showPermissionDialog(String title, int currentValue, String permissionKey) {
        String[] options = GroupPermission.getPermissionOptions();
        int currentIndex = GroupPermission.getPermissionIndex(currentValue);
        
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setSingleChoiceItems(options, currentIndex, (dialog, which) -> {
                int newValue = GroupPermission.getPermissionValue(which);
                updatePermission(permissionKey, newValue);
                dialog.dismiss();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 更新权限设置
     */
    private void updatePermission(String key, int value) {
        Map<String, Object> settings = new HashMap<>();
        settings.put(key, value);
        
        ServiceManager.getUserService().setGroupSettings(groupId, settings, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(GroupManagementActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                
                // 更新本地数据
                updateLocalPermission(key, value);
                updateUI();
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(GroupManagementActivity.this, 
                    "设置失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 更新本地权限数据
     */
    private void updateLocalPermission(String key, int value) {
        if (groupManagement == null) return;
        
        switch (key) {
            case GroupPermission.KEY_ADD_MEMBER:
                groupManagement.setGroupAddMemberRight(value);
                break;
            case GroupPermission.KEY_TOP_MSG:
                groupManagement.setGroupTopMsgRight(value);
                break;
            case GroupPermission.KEY_MENTION_ALL:
                groupManagement.setGroupMentionAllRight(value);
                break;
            case GroupPermission.KEY_EDIT_MSG:
                groupManagement.setGroupEditMsgRight(value);
                break;
            case GroupPermission.KEY_SEND_MSG:
                groupManagement.setGroupSendMsgRight(value);
                break;
        }
    }
    
    /**
     * 更新历史消息设置
     */
    private void updateHistoryMessageSetting(boolean visible) {
        ServiceManager.getUserService().setGroupHistoryMessageVisible(groupId, visible, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(GroupManagementActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                groupManagement.setHistoryMessageVisible(visible ? 1 : 0);
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(GroupManagementActivity.this, 
                    "设置失败: " + message, Toast.LENGTH_SHORT).show();
                // 恢复开关状态
                switchHistoryMessage.setChecked(!visible);
            }
        });
    }
    
    /**
     * 显示解散群组确认对话框
     */
    private void showDismissGroupDialog() {
        new AlertDialog.Builder(this)
            .setTitle("解散群组")
            .setMessage("解散后，所有成员将被移出群组，且无法恢复。确定要解散群组吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                dismissGroup();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 解散群组
     */
    private void dismissGroup() {
        ServiceManager.getUserService().dissolveGroup(groupId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // 使用当前会话列表中的 Conversation 对象，确保包含正确的 subChannel
                IConversationManager cm = JIM.getInstance().getConversationManager();
                Conversation targetConv = new Conversation(Conversation.ConversationType.GROUP, groupId);
                try {
                    List<ConversationInfo> all = cm.getConversationInfoList();
                    if (all != null) {
                        for (ConversationInfo info : all) {
                            if (info == null || info.getConversation() == null) continue;
                            Conversation c = info.getConversation();
                            if (c.getConversationType() == Conversation.ConversationType.GROUP
                                    && groupId.equals(c.getConversationId())) {
                                targetConv = c;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w("GroupManagement", "find conversation for delete failed", e);
                }

                cm.deleteConversationInfo(
                        targetConv,
                        new IConversationManager.ISimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(GroupManagementActivity.this, "群组已解散", Toast.LENGTH_SHORT).show();
                                // 将该会话加入本地隐藏列表，防止后续同步重新出现在会话列表
                                HiddenConversationStore.addHidden(GroupManagementActivity.this, groupId);
                                // 主动通知会话列表移除该会话，保证 UI 一定更新
                                EventBus.getDefault().post(new ConversationIdDeletedEvent(groupId));
                                // 导航回到消息列表页面
                                Intent intent = new Intent(GroupManagementActivity.this, com.juggle.im.android.app.MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError(int errorCode) {
                                Log.e("GroupManagement",
                                        "deleteConversationInfo failed, code=" + errorCode);
                                Toast.makeText(GroupManagementActivity.this,
                                        R.string.operation_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(GroupManagementActivity.this, R.string.operation_failed,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
