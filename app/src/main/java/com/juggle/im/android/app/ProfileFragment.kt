package com.juggle.im.android.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.juggle.im.android.widget.ProfileView
import com.juggle.im.android.widget.ProfileMenuView
import com.juggle.im.android.theme.ThemeManager

/**
 * 个人资料页面 Fragment
 * 显示用户头像、昵称、个性签名、账号信息和统计数据
 * 需求: 8.1, 8.5
 */
class ProfileFragment : Fragment() {
    
    private lateinit var profileView: ProfileView
    private lateinit var menuView: ProfileMenuView
    private lateinit var themeManager: ThemeManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            
            themeManager = ThemeManager.getInstance(requireContext())
            profileView = ProfileView(requireContext())
            
            // 绑定示例数据
            val profileData = ProfileView.ProfileData(
                userId = "user_001",
                nickname = "用户昵称",
                avatar = "https://via.placeholder.com/120",
                signature = "这是我的个性签名",
                accountId = "user_account_001",
                friendCount = 50,
                groupCount = 10,
                momentCount = 25
            )
            
            profileView.bindData(profileData)
            profileView.setOnEditClickListener {
                // 跳转到编辑资料页面
                startActivity(Intent(requireContext(), EditProfileActivity::class.java))
            }
            
            container.addView(profileView)
            
            // 添加功能入口菜单
            menuView = ProfileMenuView(requireContext())
            
            // 添加设置菜单项
            menuView.addMenuItem(ProfileMenuView.MenuItem("settings", "设置")) {
                // 处理设置点击
            }
            
            // 添加关于菜单项
            menuView.addMenuItem(ProfileMenuView.MenuItem("about", "关于")) {
                // 处理关于点击
            }
            
            // 添加帮助菜单项
            menuView.addMenuItem(ProfileMenuView.MenuItem("help", "帮助")) {
                // 处理帮助点击
            }
            
            container.addView(menuView)
            addView(container)
        }
    }
    
    /**
     * 获取菜单视图（用于测试）
     */
    fun getMenuView(): ProfileMenuView = menuView
    
    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }
}
