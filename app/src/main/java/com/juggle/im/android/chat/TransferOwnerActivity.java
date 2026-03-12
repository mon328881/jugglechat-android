package com.juggle.im.android.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juggle.im.android.R;
import com.juggle.im.android.server.beans.GroupDetailBean;
import com.juggle.im.android.server.beans.GroupMemberBean;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 转让群主页面
 */
public class TransferOwnerActivity extends AppCompatActivity {
    private static final String TAG = "TransferOwnerActivity";
    private static final String EXTRA_GROUP_ID = "group_id";
    
    private String groupId;
    private int myRole = GroupRole.MEMBER;
    private List<GroupMemberBean> members = new ArrayList<>();
    private GroupMemberAdapter adapter;
    private ListView lvMembers;

    public static Intent intentFor(Context ctx, String groupId) {
        Intent i = new Intent(ctx, TransferOwnerActivity.class);
        i.putExtra(EXTRA_GROUP_ID, groupId);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_owner);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        
        initViews();
        loadGroupInfo();
    }
    
    private void initViews() {
        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        
        lvMembers = findViewById(R.id.lv_members);
        adapter = new GroupMemberAdapter(this, members, this::onMemberSelected);
        lvMembers.setAdapter(adapter);
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
                
                // 检查权限
                if (!GroupRole.isOwner(myRole)) {
                    Toast.makeText(TransferOwnerActivity.this, 
                        "只有群主可以转让群主身份", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                members.clear();
                if (data.getMembers() != null) {
                    // 过滤掉群主本身
                    for (GroupMemberBean member : data.getMembers()) {
                        if (member.getRole() != GroupRole.OWNER) {
                            members.add(member);
                        }
                    }
                }
                
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "加载群组信息失败: " + code + " " + message);
                Toast.makeText(TransferOwnerActivity.this, 
                    "加载群组信息失败: " + message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    /**
     * 成员被选中
     */
    private void onMemberSelected(GroupMemberBean member) {
        showConfirmDialog(member);
    }
    
    /**
     * 显示确认对话框
     */
    private void showConfirmDialog(GroupMemberBean member) {
        new AlertDialog.Builder(this)
            .setTitle("转让群主")
            .setMessage("确定要将群主身份转让给 " + member.getNickname() + " 吗？\n转让后，你将成为普通成员。")
            .setPositiveButton("确定", (dialog, which) -> {
                transferOwner(member.getUserId());
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 转让群主
     */
    private void transferOwner(String newOwnerId) {
        ServiceManager.getUserService().transferGroupOwner(groupId, newOwnerId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(TransferOwnerActivity.this, "群主转让成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "转让群主失败: " + code + " " + message);
                Toast.makeText(TransferOwnerActivity.this, 
                    "转让群主失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
