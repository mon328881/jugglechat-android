package com.juggle.im.android.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;

/**
 * 群组管理设置页面
 * 用于管理群组权限、管理员等设置
 */
public class GroupManagementActivity extends AppCompatActivity {
    private static final String EXTRA_GROUP_ID = "group_id";
    private String groupId;

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

        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 谁可以添加成员
        findViewById(R.id.ll_add_member_permission).setOnClickListener(v -> {
            Toast.makeText(this, "添加成员权限设置功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 谁可以置顶消息
        findViewById(R.id.ll_pin_message_permission).setOnClickListener(v -> {
            Toast.makeText(this, "置顶消息权限设置功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 谁可以 @ 所有人
        findViewById(R.id.ll_mention_all_permission).setOnClickListener(v -> {
            Toast.makeText(this, "@所有人权限设置功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 谁可以编辑群信息
        findViewById(R.id.ll_edit_group_info_permission).setOnClickListener(v -> {
            Toast.makeText(this, "编辑群信息权限设置功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 谁可以在群里发言
        findViewById(R.id.ll_send_message_permission).setOnClickListener(v -> {
            Toast.makeText(this, "发言权限设置功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 谁可以设置消息定时删除
        findViewById(R.id.ll_auto_delete_permission).setOnClickListener(v -> {
            Toast.makeText(this, "定时删除权限设置功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 新人进群获取历史消息开关
        findViewById(R.id.switch_history_message).setOnClickListener(v -> {
            Toast.makeText(this, "历史消息设置功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 群管理员
        findViewById(R.id.ll_group_admins).setOnClickListener(v -> {
            Toast.makeText(this, "群管理员功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 转让群主
        findViewById(R.id.ll_transfer_owner).setOnClickListener(v -> {
            Toast.makeText(this, "转让群主功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 解散群组
        findViewById(R.id.btn_dismiss_group).setOnClickListener(v -> {
            Toast.makeText(this, "解散群组功能待实现", Toast.LENGTH_SHORT).show();
        });
    }
}
