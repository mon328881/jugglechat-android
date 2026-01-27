package com.juggle.im.android.app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.juggle.im.android.widget.MaterialButton
import com.juggle.im.android.widget.MaterialTextField
import com.juggle.im.android.theme.ThemeManager

/**
 * 个人资料编辑页面
 * 提供清晰的表单，编辑昵称、个性签名、性别等信息
 * 需求: 8.3, 8.4
 */
class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var themeManager: ThemeManager
    private lateinit var nicknameField: MaterialTextField
    private lateinit var signatureField: MaterialTextField
    private lateinit var genderField: MaterialTextField
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    
    // 用于跟踪是否有改动
    private var originalNickname: String = ""
    private var originalSignature: String = ""
    private var originalGender: String = ""
    
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
        
        // 标题
        val titleView = TextView(this).apply {
            text = "编辑个人资料"
            textSize = 24f
            setTextColor(theme.colors.onBackground)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
        }
        container.addView(titleView)
        
        // 昵称输入框
        nicknameField = MaterialTextField(this).apply {
            setHint("请输入昵称")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateSaveButtonState()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        container.addView(nicknameField)
        
        // 个性签名输入框
        signatureField = MaterialTextField(this).apply {
            setHint("请输入个性签名")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.md
            }
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateSaveButtonState()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        container.addView(signatureField)
        
        // 性别输入框
        genderField = MaterialTextField(this).apply {
            setHint("请选择性别")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = theme.spacing.lg
            }
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateSaveButtonState()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        container.addView(genderField)
        
        // 按钮容器
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // 取消按钮
        cancelButton = MaterialButton(this).apply {
            setText("取消")
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                rightMargin = theme.spacing.sm
            }
            setOnClickListener {
                finish()
            }
        }
        buttonContainer.addView(cancelButton)
        
        // 保存按钮
        saveButton = MaterialButton(this).apply {
            setText("保存")
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                leftMargin = theme.spacing.sm
            }
            setOnClickListener {
                saveProfile()
            }
        }
        buttonContainer.addView(saveButton)
        
        container.addView(buttonContainer)
        mainContainer.addView(container)
        
        setContentView(mainContainer)
        
        // 初始化保存按钮状态
        updateSaveButtonState()
    }
    
    /**
     * 更新保存按钮状态
     * 有改动时启用，否则禁用
     */
    private fun updateSaveButtonState() {
        val hasChanges = nicknameField.getText() != originalNickname ||
                signatureField.getText() != originalSignature ||
                genderField.getText() != originalGender
        
        saveButton.isEnabled = hasChanges
    }
    
    /**
     * 保存个人资料
     */
    private fun saveProfile() {
        // 这里应该调用后端 API 保存数据
        // 暂时只是关闭页面
        finish()
    }
    
    /**
     * 获取昵称（用于测试）
     */
    fun getNickname(): String = nicknameField.getText()
    
    /**
     * 获取个性签名（用于测试）
     */
    fun getSignature(): String = signatureField.getText()
    
    /**
     * 获取性别（用于测试）
     */
    fun getGender(): String = genderField.getText()
    
    /**
     * 获取保存按钮是否启用（用于测试）
     */
    fun isSaveButtonEnabled(): Boolean = saveButton.isEnabled
    
    /**
     * 设置昵称（用于测试）
     */
    fun setNickname(nickname: String) {
        nicknameField.setText(nickname)
        originalNickname = nickname
    }
    
    /**
     * 设置个性签名（用于测试）
     */
    fun setSignature(signature: String) {
        signatureField.setText(signature)
        originalSignature = signature
    }
    
    /**
     * 设置性别（用于测试）
     */
    fun setGender(gender: String) {
        genderField.setText(gender)
        originalGender = gender
    }
}
