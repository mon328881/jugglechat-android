package com.juggle.im.android.chat;

import static android.view.View.GONE;
import static com.juggle.im.android.chat.ConversationActivity.*;
import static com.juggle.im.android.chat.SelectMemberActivity.DISABLE_MEMBERS;
import static com.juggle.im.android.chat.SelectMemberActivity.SELECTED_MEMBERS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;
import com.juggle.im.android.chat.component.UserListAdapter;
import com.juggle.im.android.server.beans.GroupDetailBean;
import com.juggle.im.android.server.beans.GroupMemberBean;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;
import com.juggle.im.android.utils.AvatarUtils;
import com.juggle.im.model.Conversation;
import com.juggle.im.JIM;
import com.juggle.im.model.UserInfo;

import java.util.ArrayList;

/**
 * Conversation settings page. Supports mute (do not disturb), pin to top and actions to clear chat
 * and (for groups) leave group.
 */
public class ConversationSettingsActivity extends AppCompatActivity {
    private String conversationId;
    private boolean isGroup;
    private boolean isTop, isMute;
    private ArrayList<String> groupMemberIds = new ArrayList<>();
    private boolean isGroupOwner = false; // 是否为群主


    public static Intent intentFor(Context ctx,
                                   String conversationId,
                                   String title,
                                   boolean isGroup,
                                   boolean isTop,
                                   boolean isMute) {
        Intent i = new Intent(ctx, ConversationSettingsActivity.class);
        i.putExtra(EXTRA_CONVERSATION_ID, conversationId);
        i.putExtra(EXTRA_IS_GROUP, isGroup);
        i.putExtra(EXTRA_IS_TOP, isTop);
        i.putExtra(EXTRA_IS_MUTE, isMute);
        i.putExtra(EXTRA_TITLE, title);
        return i;
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_settings);

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        isGroup = getIntent().getBooleanExtra(EXTRA_IS_GROUP, false);
        isTop = getIntent().getBooleanExtra(EXTRA_IS_TOP, false);
        isMute = getIntent().getBooleanExtra(EXTRA_IS_MUTE, false);

        // 免打扰按钮
        findViewById(R.id.btn_mute).setOnClickListener(v -> {
            Conversation.ConversationType conversationType = isGroup ? Conversation.ConversationType.GROUP : Conversation.ConversationType.PRIVATE;
            Conversation conv = new Conversation(conversationType, conversationId);
            JIM.getInstance().getConversationManager().setMute(conv, !isMute, null);
            Toast.makeText(this, !isMute ? R.string.settings_mute_on : R.string.settings_mute_off, Toast.LENGTH_SHORT).show();
            isMute = !isMute;
        });
        
        // 置顶按钮
        findViewById(R.id.btn_pin).setOnClickListener(v -> {
            Conversation.ConversationType conversationType = isGroup ? Conversation.ConversationType.GROUP : Conversation.ConversationType.PRIVATE;
            Conversation conv = new Conversation(conversationType, conversationId);
            JIM.getInstance().getConversationManager().setTop(conv, !isTop, null);
            Toast.makeText(this, !isTop ? R.string.settings_top_on : R.string.settings_top_off, Toast.LENGTH_SHORT).show();
            isTop = !isTop;
        });
        
        // 清空消息按钮
        findViewById(R.id.btn_clear_messages).setOnClickListener(v -> {
            try {
                Conversation.ConversationType conversationType = isGroup ? Conversation.ConversationType.GROUP : Conversation.ConversationType.PRIVATE;
                Conversation conv = new Conversation(conversationType, conversationId);
                JIM.getInstance().getMessageManager().clearMessages(conv, 0, null);
                Toast.makeText(this, R.string.chat_cleared, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
            }
        });
        
        // 添加成员
        findViewById(R.id.ll_add_member).setOnClickListener(v -> {
            Intent it = new Intent(this, SelectMemberActivity.class);
            it.putExtra("mode", UserListAdapter.LIST_MODE_SELECT_MEMBER);
            it.putStringArrayListExtra(DISABLE_MEMBERS, groupMemberIds);
            startActivityForResult(it, 1000);
        });
        
        // 群组成员
        findViewById(R.id.ll_group_members).setOnClickListener(v -> {
            Intent it = new Intent(this, SelectMemberActivity.class);
            it.putExtra("GROUP_ID", conversationId);
            it.putExtra("mode", UserListAdapter.LIST_MODE_NORMAL);
            startActivity(it);
        });
        
        // 群组管理
        findViewById(R.id.ll_group_management).setOnClickListener(v -> {
            Intent intent = GroupManagementActivity.intentFor(this, conversationId);
            startActivity(intent);
        });

        // 退出群组按钮
        Button btnLeave = findViewById(R.id.btn_leave_group);
        if (isGroup) {
            btnLeave.setVisibility(View.VISIBLE);
            btnLeave.setOnClickListener(v -> {
                try {
                    Toast.makeText(this, R.string.left_group, Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            btnLeave.setVisibility(GONE);
        }

        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 编辑按钮（仅群主可见）
        ImageView ivEdit = findViewById(R.id.iv_edit);
        ivEdit.setVisibility(GONE); // 默认隐藏，等待加载群组信息后判断

        if (isGroup) {
            // 显示群组相关控件
            findViewById(R.id.ll_add_member).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_group_members).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_group_management).setVisibility(View.VISIBLE);
            
            // 加载群组信息
            ServiceManager.getUserService().getGroupInfo(conversationId, new ApiCallback<GroupDetailBean>() {
                @Override
                public void onSuccess(GroupDetailBean data) {
                    ImageView groupAvatar = findViewById(R.id.iv_group_avatar);
                    AvatarUtils.loadAvatar(groupAvatar, data.getPortrait(), data.getGroupName());
                    TextView groupName = findViewById(R.id.tv_group_name);
                    groupName.setText(data.getGroupName());
                    
                    TextView memberCount = findViewById(R.id.tv_member_count);
                    memberCount.setText(data.getMembers().size() + " 个成员");

                    for (GroupMemberBean member : data.getMembers()) {
                        groupMemberIds.add(member.getUserId());
                    }
                    
                    // 判断当前用户是否为群主（myRole == 1 表示群主）
                    isGroupOwner = (data.getMyRole() == 1);
                    
                    // 只有群主才能看到编辑按钮
                    ImageView ivEdit = findViewById(R.id.iv_edit);
                    if (isGroupOwner) {
                        ivEdit.setVisibility(View.VISIBLE);
                        ivEdit.setOnClickListener(v -> {
                            TextView groupNameView = findViewById(R.id.tv_group_name);
                            String currentName = groupNameView.getText().toString();
                            Intent intent = new Intent(ConversationSettingsActivity.this, EditNicknameActivity.class);
                            intent.putExtra("current_nickname", currentName);
                            startActivityForResult(intent, 2000);
                        });
                    } else {
                        ivEdit.setVisibility(GONE);
                    }
                }

                @Override
                public void onError(int code, String message) {
                    Log.e("getGroupInfo", code + message);
                }
            });
        } else {
            // 隐藏群组相关控件
            findViewById(R.id.ll_add_member).setVisibility(GONE);
            findViewById(R.id.ll_group_members).setVisibility(GONE);
            findViewById(R.id.ll_group_management).setVisibility(GONE);

            ImageView groupAvatar = findViewById(R.id.iv_group_avatar);
            UserInfo data = JIM.getInstance().getUserInfoManager().getUserInfo(conversationId);
            if (data != null) {
                AvatarUtils.loadAvatar(groupAvatar, data.getPortrait(), data.getUserName());
                TextView tvName = findViewById(R.id.tv_group_name);
                tvName.setText(data.getUserName());
                TextView memberCount = findViewById(R.id.tv_member_count);
                memberCount.setVisibility(GONE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            ArrayList<String> selectedMemberIds = data.getStringArrayListExtra(SELECTED_MEMBERS);
            ServiceManager.getUserService().inviteJoinGroup(conversationId, selectedMemberIds, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Toast.makeText(ConversationSettingsActivity.this, "邀请成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int code, String message) {
                    Log.e("inviteJoinGroup", code + message);
                    Toast.makeText(ConversationSettingsActivity.this, "邀请失败" + code, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (requestCode == 2000 && resultCode == RESULT_OK) {
            // 处理群组重命名
            String newGroupName = data.getStringExtra("new_nickname");
            if (!android.text.TextUtils.isEmpty(newGroupName)) {
                // 更新UI
                TextView groupName = findViewById(R.id.tv_group_name);
                groupName.setText(newGroupName);
                
                // TODO: 调用API更新群组名称
                Toast.makeText(this, "群组名称已更新为: " + newGroupName, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
