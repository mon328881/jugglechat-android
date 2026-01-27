package com.juggle.im.android.app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.juggle.im.android.widget.MaterialButton
import com.juggle.im.android.widget.ProfileView
import com.juggle.im.android.theme.ThemeManager

/**
 * 其他用户资料页面
 * 显示添加朋友、发送消息等操作按钮
 * 需求: 8.6
 */
class OtherUserProfileActivity : AppCompatActivity() {
    
    private lateinit var themeManager: ThemeManager
    private lateinit var profileView: ProfileView
    private lateinit var addFriendButton: MaterialButton
    private lateinit var sendMessageButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        themeManager = ThemeManager.getInstance(this)
        val theme = themeManager.getCurrentTheme()
        
        // 创建主容器
        val mainContainer = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(theme.spacing.lg, theme.spacing.lg, theme.spacing.lg, theme.spacing.lg)
        }
        
        // 个人资料视图
        profileView = ProfileView(this)
        
        // 绑定示例数据
        val profileData = ProfileView.ProfileData(
            userId = "user_002",
            nickname = "其他用户",
            avatar = "https://via.placeholder.com/120",
            signature = "这是其他用户的个性签名",
            accountId = "other_user_account",
            friendCount = 100,
            groupCount = 20,
            momentCount = 50
        )
        
        profileView.bindData(profileData)
        container.addView(profileView)
        
        // 操作按钮容器
        val actionContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = theme.spacing.lg
            }
        }
        
        // 添加朋友按钮
        addFriendButton = MaterialButton(this).apply {
            setText("添加朋友")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            setOnClickListener {
                // 处理添加朋友逻辑
            }
        }
        actionContainer.addView(addFriendButton)
        
        // 发送消息按钮
        sendMessageButton = MaterialButton(this).apply {
            setText("发送消息")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                // 处理发送消息逻辑
            }
        }
        actionContainer.addView(sendMessageButton)
        
        container.addView(actionContainer)
        mainContainer.addView(container)
        
        setContentView(mainContainer)
    }
    
    /**
     * 获取添加朋友按钮（用于测试）
     */
    fun getAddFriendButton(): MaterialButton = addFriendButton
    
    /**
     * 获取发送消息按钮（用于测试）
     */
    fun getSendMessageButton(): MaterialButton = sendMessageButton
}
