package com.juggle.im.android.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.juggle.im.android.R;
import com.juggle.im.android.server.beans.GroupDetailBean;
import com.juggle.im.android.server.beans.GroupMemberBean;
import com.juggle.im.android.server.http.ApiCallback;
import com.juggle.im.android.server.http.ServiceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 群管理员管理页面
 */
public class GroupAdminsActivity extends AppCompatActivity {
    private static final String TAG = "GroupAdminsActivity";
    private static final String EXTRA_GROUP_ID = "group_id";
    
    private String groupId;
    private int myRole = GroupRole.MEMBER;
    private List<GroupMemberBean> allMembers = new ArrayList<>();
    private List<GroupMemberBean> adminMembers = new ArrayList<>();
    private GroupAdminAdapter adapter;
    private ListView lvAdmins;

    public static Intent intentFor(Context ctx, String groupId) {
        Intent i = new Intent(ctx, GroupAdminsActivity.class);
        i.putExtra(EXTRA_GROUP_ID, groupId);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_admins);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        
        initViews();
        loadGroupInfo();
    }
    
    private void initViews() {
        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        
        lvAdmins = findViewById(R.id.lv_admins);
        adapter = new GroupAdminAdapter(this, adminMembers, this::onAdminRemoveClick);
        lvAdmins.setAdapter(adapter);
        
        // 添加管理员按钮 - 点击整个区域
        findViewById(R.id.tv_add_admin).setOnClickListener(v -> {
            if (!GroupRole.isOwner(myRole)) {
                Toast.makeText(this, "只有群主可以添加管理员", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddAdminDialog();
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
                allMembers.clear();
                if (data.getMembers() != null) {
                    allMembers.addAll(data.getMembers());
                }
                
                // 过滤管理员
                filterAdmins();
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "加载群组信息失败: " + code + " " + message);
                Toast.makeText(GroupAdminsActivity.this, 
                    "加载群组信息失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 过滤管理员列表
     */
    private void filterAdmins() {
        adminMembers.clear();
        for (GroupMemberBean member : allMembers) {
            if (member.getRole() == GroupRole.ADMIN) {
                adminMembers.add(member);
            }
        }
        adapter.notifyDataSetChanged();
    }
    
    /**
     * 显示添加管理员对话框
     */
    private void showAddAdminDialog() {
        // 获取非管理员的成员列表
        List<GroupMemberBean> nonAdminMembers = new ArrayList<>();
        for (GroupMemberBean member : allMembers) {
            if (member.getRole() != GroupRole.ADMIN && member.getRole() != GroupRole.OWNER) {
                nonAdminMembers.add(member);
            }
        }
        
        if (nonAdminMembers.isEmpty()) {
            Toast.makeText(this, "没有可添加的成员", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建底部弹出框
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_add_admin_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);
        
        // 初始化UI
        EditText etSearch = sheetView.findViewById(R.id.et_search);
        ListView lvMembers = sheetView.findViewById(R.id.lv_members);
        TextView tvConfirm = sheetView.findViewById(R.id.tv_confirm);
        View ivClose = sheetView.findViewById(R.id.iv_close);
        
        // 创建适配器
        SelectAdminAdapter adapter = new SelectAdminAdapter(this, nonAdminMembers);
        lvMembers.setAdapter(adapter);
        
        // 列表项点击事件 - 直接在整个行上点击时选中
        lvMembers.setOnItemClickListener((parent, view, position, id) -> {
            adapter.setSelectedPosition(position);
            adapter.notifyDataSetChanged();
        });
        
        // 关闭按钮
        ivClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        // 确定按钮
        tvConfirm.setOnClickListener(v -> {
            int selectedPosition = adapter.getSelectedPosition();
            Log.d(TAG, "选中位置: " + selectedPosition + ", 列表大小: " + nonAdminMembers.size());
            if (selectedPosition >= 0 && selectedPosition < nonAdminMembers.size()) {
                GroupMemberBean selectedMember = nonAdminMembers.get(selectedPosition);
                setMemberAsAdmin(selectedMember.getUserId());
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(this, "请选择一个成员", Toast.LENGTH_SHORT).show();
            }
        });
        
        bottomSheetDialog.show();
    }
    
    /**
     * 设置成员为管理员
     */
    private void setMemberAsAdmin(String memberId) {
        List<String> adminIds = new ArrayList<>();
        adminIds.add(memberId);
        
        ServiceManager.getUserService().addGroupAdministrators(groupId, adminIds, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(GroupAdminsActivity.this, "添加管理员成功", Toast.LENGTH_SHORT).show();
                // 重新加载群组信息
                loadGroupInfo();
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "添加管理员失败: " + code + " " + message);
                Toast.makeText(GroupAdminsActivity.this, 
                    "添加管理员失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 移除管理员
     */
    private void onAdminRemoveClick(GroupMemberBean member) {
        new AlertDialog.Builder(this)
            .setTitle("移除管理员")
            .setMessage("确定要移除 " + member.getNickname() + " 的管理员身份吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                removeMemberAsAdmin(member.getUserId());
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 移除成员的管理员身份
     */
    private void removeMemberAsAdmin(String memberId) {
        List<String> adminIds = new ArrayList<>();
        adminIds.add(memberId);
        
        ServiceManager.getUserService().delGroupAdministrators(groupId, adminIds, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(GroupAdminsActivity.this, "移除管理员成功", Toast.LENGTH_SHORT).show();
                // 重新加载群组信息
                loadGroupInfo();
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "移除管理员失败: " + code + " " + message);
                Toast.makeText(GroupAdminsActivity.this, 
                    "移除管理员失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
