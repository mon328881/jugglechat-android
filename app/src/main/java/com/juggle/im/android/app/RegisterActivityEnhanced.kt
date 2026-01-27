package com.juggle.im.android.app

import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Button
import android.view.View

/**
 * 注册界面增强功能
 * 添加进度指示、表单验证和加载状态管理
 */
class RegisterActivityEnhanced {
    
    companion object {
        private const val TOTAL_STEPS = 3
    }
    
    private var currentStep = 1
    
    /**
     * 初始化进度指示器
     * 验证: 需求 2.3, 2.5
     */
    fun initializeProgressIndicator(
        progressContainer: LinearLayout?,
        progressBar: ProgressBar?,
        progressText: TextView?
    ) {
        updateProgressDisplay(progressContainer, progressBar, progressText)
    }
    
    /**
     * 更新进度显示
     */
    private fun updateProgressDisplay(
        progressContainer: LinearLayout?,
        progressBar: ProgressBar?,
        progressText: TextView?
    ) {
        progressText?.text = "第 $currentStep/$TOTAL_STEPS 步"
        val progress = (currentStep * 100) / TOTAL_STEPS
        progressBar?.progress = progress
    }
    
    /**
     * 设置加载状态
     * 验证: 需求 2.5
     */
    fun setLoadingState(
        isLoading: Boolean,
        registerButton: Button?,
        getCodeButton: Button?,
        inputField: EditText?,
        passwordInput: EditText?,
        verificationCode: EditText?,
        registerTypeGroup: RadioGroup?,
        progressContainer: LinearLayout?,
        progressBar: ProgressBar?,
        progressText: TextView?
    ) {
        registerButton?.isEnabled = !isLoading
        getCodeButton?.isEnabled = !isLoading
        inputField?.isEnabled = !isLoading
        passwordInput?.isEnabled = !isLoading
        verificationCode?.isEnabled = !isLoading
        registerTypeGroup?.isEnabled = !isLoading
        
        if (isLoading) {
            registerButton?.alpha = 0.6f
            progressContainer?.visibility = View.VISIBLE
        } else {
            registerButton?.alpha = 1.0f
            progressContainer?.visibility = View.GONE
        }
    }
    
    /**
     * 更新步骤
     */
    fun updateStep(
        step: Int,
        progressContainer: LinearLayout?,
        progressBar: ProgressBar?,
        progressText: TextView?
    ) {
        currentStep = step
        updateProgressDisplay(progressContainer, progressBar, progressText)
    }
    
    /**
     * 验证注册表单
     */
    fun validateRegisterForm(
        accountType: Int,
        input: String,
        code: String,
        password: String,
        accountRadioId: Int,
        phoneRadioId: Int,
        emailRadioId: Int
    ): Pair<Boolean, String> {
        return when {
            input.isEmpty() -> Pair(false, "请输入账号")
            password.isEmpty() -> Pair(false, "请输入密码")
            accountType == accountRadioId && (input.length < 5 || input.length > 20) -> {
                Pair(false, "账号长度应为5-20个字符")
            }
            (accountType == phoneRadioId || accountType == emailRadioId) && code.isEmpty() -> {
                Pair(false, "请输入验证码")
            }
            password.length < 6 -> Pair(false, "密码长度不能少于 6 位")
            else -> Pair(true, "")
        }
    }
}
