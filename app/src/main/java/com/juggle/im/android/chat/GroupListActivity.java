package com.juggle.im.android.chat;

import android.content.Intent;
import android.os.Bundle;
import com.juggle.im.android.utils.LogUtil;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.JIM;
import com.juggle.im.android.R;
import com.juggle.im.android.chat.component.GroupListAdapter;
import com.juggle.im.android.server.beans.GroupBean;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GroupInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GroupListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        initViews();
        setupListeners();
        loadGroups();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_group_list);
        adapter = new GroupListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 设置群组点击监听
        adapter.setOnGroupClickListener(group -> {
            // 直接跳转到群组会话（显示该群组的聊天界面）
            Intent intent = ConversationActivity.intentFor(GroupListActivity.this,
                    group.getGroup_id(),
                    group.getGroup_name(),
                    true,  // isGroup
                    false, // isTop
                    false);// isMute
            startActivity(intent);
            finish();  // 关闭群组列表页面
        });
    }

    private void setupListeners() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadGroups() {
        // 从 IM SDK 的会话列表中获取群组
        List<ConversationInfo> conversationList = JIM.getInstance().getConversationManager().getConversationInfoList();
        List<GroupBean> groups = new ArrayList<>();
        
        // 获取当前用户ID
        String currentUserId = JIM.getInstance().getCurrentUserId();

        if (conversationList != null && !conversationList.isEmpty()) {
            for (ConversationInfo conversationInfo : conversationList) {
                Conversation conversation = conversationInfo.getConversation();
                if (conversation != null && conversation.getConversationType() == Conversation.ConversationType.GROUP) {
                    String groupId = conversation.getConversationId();
                    
                    // 从 IM SDK 获取群组信息
                    GroupInfo groupInfo = JIM.getInstance().getUserInfoManager().getGroupInfo(groupId);
                    if (groupInfo != null) {
                        GroupBean groupBean = new GroupBean();
                        groupBean.setGroup_id(groupId);
                        groupBean.setGroup_name(groupInfo.getGroupName());
                        groupBean.setGroup_portrait(groupInfo.getPortrait());
                        
                        // 从 Extra 字段中获取创建者信息（后端现在返回 creator_id 在 Extra 中）
                        String creatorId = null;
                        Map<String, String> extra = groupInfo.getExtra();
                        if (extra != null) {
                            creatorId = extra.get("creator_id");
                            if (creatorId != null) {
                                LogUtil.d("GroupListActivity", "Group loaded: " + groupInfo.getGroupName());
                            }
                        }
                        
                        groupBean.setCreator_id(creatorId);
                        
                        // 判断当前用户是否是群组创建者
                        boolean isCreator = currentUserId != null && creatorId != null && currentUserId.equals(creatorId);
                        groupBean.setIs_creator(isCreator);
                        
                        groups.add(groupBean);
                    }
                }
            }
        }

        if (!groups.isEmpty()) {
            adapter.setGroups(groups);
            LogUtil.d("GroupListActivity", "加载群组成功，共 " + groups.size() + " 个");
        } else {
            Toast.makeText(GroupListActivity.this, "暂无群组", Toast.LENGTH_SHORT).show();
        }
    }
}

