package com.juggle.im.android.app

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.juggle.im.android.widget.MaterialButton
import com.juggle.im.android.widget.MaterialTextField
import com.juggle.im.android.theme.ThemeManager

/**
 * 群组设置界面
 * 允许群主修改群组名称、描述、头像等信息
 * 
 * **验证: 需求 9.5**
 */
class GroupSettingsActivity : AppCompatActivity() {
    
    /**
     * 群组设置数据类
     */
    data class GroupSettings(
        val id: String,
        val name: String,
        val description: String,
        val avatar: String
    )
    
    private lateinit var themeManager: ThemeManager
    private lateinit var groupAvatarView: ImageView
    private lateinit var groupNameField: MaterialTextField
    private lateinit var groupDescriptionField: MaterialTextField
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    
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
        
        // 群组名称标签
        val nameLabel = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.sm
            }
            text = "群组名称"
            textSize = 14f
            setTextColor(theme.colors.onBackground)
        }
        container.addView(nameLabel)
        
        // 群组名称输入框
        groupNameField = MaterialTextField(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
            setHint("请输入群组名称")
        }
        container.addView(groupNameField)
        
        // 群组描述标签
        val descriptionLabel = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.sm
            }
            text = "群组描述"
            textSize = 14f
            setTextColor(theme.colors.onBackground)
        }
        container.addView(descriptionLabel)
        
        // 群组描述输入框
        groupDescriptionField = MaterialTextField(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
            setHint("请输入群组描述")
        }
        container.addView(groupDescriptionField)
        
        // 操作按钮容器
        val actionContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // 保存按钮
        saveButton = MaterialButton(this).apply {
            setText("保存")
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = theme.spacing.sm
            }
            setOnClickListener {
                // 处理保存逻辑
            }
        }
        actionContainer.addView(saveButton)
        
        // 取消按钮
        cancelButton = MaterialButton(this).apply {
            setText("取消")
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = theme.spacing.sm
            }
            setOnClickListener {
                // 处理取消逻辑
                finish()
            }
        }
        actionContainer.addView(cancelButton)
        
        container.addView(actionContainer)
        mainContainer.addView(container)
        
        setContentView(mainContainer)
    }
    
    /**
     * 绑定群组设置数据
     */
    fun bindGroupSettings(settings: GroupSettings) {
        // 加载群组头像
        Glide.with(this)
            .load(settings.avatar)
            .circleCrop()
            .into(groupAvatarView)
        
        // 设置群组名称
        groupNameField.setText(settings.name)
        
        // 设置群组描述
        groupDescriptionField.setText(settings.description)
    }
    
    /**
     * 获取群组名称（用于测试）
     */
    fun getGroupName(): String = groupNameField.getText()
    
    /**
     * 设置群组名称（用于测试）
     */
    fun setGroupName(name: String) {
        groupNameField.setText(name)
    }
    
    /**
     * 获取群组描述（用于测试）
     */
    fun getGroupDescription(): String = groupDescriptionField.getText()
    
    /**
     * 设置群组描述（用于测试）
     */
    fun setGroupDescription(description: String) {
        groupDescriptionField.setText(description)
    }
    
    /**
     * 获取保存按钮（用于测试）
     */
    fun getSaveButton(): MaterialButton = saveButton
    
    /**
     * 获取取消按钮（用于测试）
     */
    fun getCancelButton(): MaterialButton = cancelButton
}
