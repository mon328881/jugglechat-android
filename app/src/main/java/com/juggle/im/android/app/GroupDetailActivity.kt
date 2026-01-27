package com.juggle.im.android.app

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.juggle.im.android.widget.MaterialButton
import com.juggle.im.android.theme.ThemeManager

/**
 * 群组详情页面
 * 显示群组信息、成员列表和群组设置
 * 
 * **验证: 需求 9.2**
 */
class GroupDetailActivity : AppCompatActivity() {
    
    /**
     * 群组详情数据类
     */
    data class GroupDetail(
        val id: String,
        val name: String,
        val avatar: String,
        val description: String,
        val memberCount: Int,
        val createdTime: String,
        val isAdmin: Boolean = false
    )
    
    private lateinit var themeManager: ThemeManager
    private lateinit var groupAvatarView: ImageView
    private lateinit var groupNameView: TextView
    private lateinit var groupDescriptionView: TextView
    private lateinit var memberCountView: TextView
    private lateinit var createdTimeView: TextView
    private lateinit var memberListView: TextView
    private lateinit var settingsButton: MaterialButton
    private lateinit var addMemberButton: MaterialButton
    
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
        
        // 群组头像
        groupAvatarView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                120,
                120
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                bottomMargin = theme.spacing.lg
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        container.addView(groupAvatarView)
        
        // 群组名称
        groupNameView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            textSize = 20f
            setTextColor(theme.colors.onBackground)
        }
        container.addView(groupNameView)
        
        // 群组描述
        groupDescriptionView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            textSize = 14f
            setTextColor(theme.colors.hint)
        }
        container.addView(groupDescriptionView)
        
        // 成员数
        memberCountView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.sm
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
        }
        container.addView(memberCountView)
        
        // 创建时间
        createdTimeView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
        }
        container.addView(createdTimeView)
        
        // 成员列表标题
        val memberListTitleView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            text = "群组成员"
            textSize = 16f
            setTextColor(theme.colors.onBackground)
        }
        container.addView(memberListTitleView)
        
        // 成员列表
        memberListView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
            textSize = 12f
            setTextColor(theme.colors.hint)
        }
        container.addView(memberListView)
        
        // 操作按钮容器
        val actionContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // 添加成员按钮
        addMemberButton = MaterialButton(this).apply {
            setText("添加成员")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            setOnClickListener {
                // 处理添加成员逻辑
            }
        }
        actionContainer.addView(addMemberButton)
        
        // 群组设置按钮
        settingsButton = MaterialButton(this).apply {
            setText("群组设置")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                // 处理群组设置逻辑
            }
        }
        actionContainer.addView(settingsButton)
        
        container.addView(actionContainer)
        mainContainer.addView(container)
        
        setContentView(mainContainer)
    }
    
    /**
     * 绑定群组详情数据
     */
    fun bindGroupData(groupDetail: GroupDetail) {
        // 加载群组头像
        Glide.with(this)
            .load(groupDetail.avatar)
            .circleCrop()
            .into(groupAvatarView)
        
        // 设置群组名称
        groupNameView.text = groupDetail.name
        
        // 设置群组描述
        groupDescriptionView.text = groupDetail.description
        
        // 设置成员数
        memberCountView.text = "成员数：${groupDetail.memberCount}"
        
        // 设置创建时间
        createdTimeView.text = "创建时间：${groupDetail.createdTime}"
        
        // 设置成员列表（示例）
        memberListView.text = "群组成员列表将在此显示"
    }
    
    /**
     * 获取群组名称（用于测试）
     */
    fun getGroupName(): String = groupNameView.text.toString()
    
    /**
     * 设置群组名称（用于测试）
     */
    fun setGroupName(name: String) {
        groupNameView.text = name
    }
    
    /**
     * 获取群组描述（用于测试）
     */
    fun getGroupDescription(): String = groupDescriptionView.text.toString()
    
    /**
     * 设置群组描述（用于测试）
     */
    fun setGroupDescription(description: String) {
        groupDescriptionView.text = description
    }
    
    /**
     * 获取成员数（用于测试）
     */
    fun getMemberCount(): String = memberCountView.text.toString()
    
    /**
     * 设置成员数（用于测试）
     */
    fun setMemberCount(count: Int) {
        memberCountView.text = "成员数：$count"
    }
    
    /**
     * 获取创建时间（用于测试）
     */
    fun getCreatedTime(): String = createdTimeView.text.toString()
    
    /**
     * 设置创建时间（用于测试）
     */
    fun setCreatedTime(time: String) {
        createdTimeView.text = "创建时间：$time"
    }
    
    /**
     * 获取添加成员按钮（用于测试）
     */
    fun getAddMemberButton(): MaterialButton = addMemberButton
    
    /**
     * 获取群组设置按钮（用于测试）
     */
    fun getSettingsButton(): MaterialButton = settingsButton
}
