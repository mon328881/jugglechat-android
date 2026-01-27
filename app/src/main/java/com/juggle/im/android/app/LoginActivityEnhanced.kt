package com.juggle.im.android.app

import android.content.Context
import android.content.SharedPreferences
import android.widget.CheckBox
import android.widget.TextView
import android.widget.EditText
import com.juggle.im.android.utils.AccountTypeDetector

/**
 * 登录界面增强功能
 * 添加账号类型自动识别和"记住账号"功能
 */
class LoginActivityEnhanced {
    
    companion object {
        private const val PREFS_NAME = "login_prefs"
        private const val KEY_REMEMBER_ACCOUNT = "remember_account"
        private const val KEY_SAVED_ACCOUNT = "saved_account"
        private const val KEY_ACCOUNT_TYPE = "account_type"
    }
    
    /**
     * 初始化登录界面增强功能
     * 
     * @param context 上下文
     * @param accountInput 账号输入框
     * @param accountTypeHint 账号类型提示文本
     * @param rememberCheckBox 记住账号复选框
     */
    fun initializeLoginEnhancements(
        context: Context,
        accountInput: EditText,
        accountTypeHint: TextView,
        rememberCheckBox: CheckBox
    ) {
        // 加载保存的账号
        loadSavedAccount(context, accountInput, rememberCheckBox)
        
        // 设置账号输入框的文本变化监听
        accountInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 更新账号类型提示
                updateAccountTypeHint(s.toString(), accountTypeHint)
            }
            
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
    
    /**
     * 更新账号类型提示
     * 
     * @param account 账号字符串
     * @param hintView 提示文本视图
     */
    private fun updateAccountTypeHint(account: String, hintView: TextView) {
        if (account.isEmpty()) {
            hintView.text = ""
            return
        }
        
        val accountType = AccountTypeDetector.detectAccountType(account)
        val typeName = AccountTypeDetector.getAccountTypeDisplayName(accountType)
        
        hintView.text = when (accountType) {
            AccountTypeDetector.AccountType.UNKNOWN -> "账号格式不正确"
            else -> "账号类型: $typeName"
        }
    }
    
    /**
     * 保存账号（如果用户选择了"记住账号"）
     * 
     * @param context 上下文
     * @param account 账号字符串
     * @param rememberCheckBox 记住账号复选框
     */
    fun saveAccountIfNeeded(
        context: Context,
        account: String,
        rememberCheckBox: CheckBox
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        if (rememberCheckBox.isChecked) {
            val accountType = AccountTypeDetector.detectAccountType(account)
            editor.putBoolean(KEY_REMEMBER_ACCOUNT, true)
            editor.putString(KEY_SAVED_ACCOUNT, account)
            editor.putString(KEY_ACCOUNT_TYPE, accountType.name)
        } else {
            editor.putBoolean(KEY_REMEMBER_ACCOUNT, false)
            editor.remove(KEY_SAVED_ACCOUNT)
            editor.remove(KEY_ACCOUNT_TYPE)
        }
        
        editor.apply()
    }
    
    /**
     * 加载保存的账号
     * 
     * @param context 上下文
     * @param accountInput 账号输入框
     * @param rememberCheckBox 记住账号复选框
     */
    private fun loadSavedAccount(
        context: Context,
        accountInput: EditText,
        rememberCheckBox: CheckBox
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rememberAccount = prefs.getBoolean(KEY_REMEMBER_ACCOUNT, false)
        
        if (rememberAccount) {
            val savedAccount = prefs.getString(KEY_SAVED_ACCOUNT, "")
            if (!savedAccount.isNullOrEmpty()) {
                accountInput.setText(savedAccount)
                rememberCheckBox.isChecked = true
            }
        }
    }
    
    /**
     * 清除保存的账号
     * 
     * @param context 上下文
     */
    fun clearSavedAccount(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(KEY_REMEMBER_ACCOUNT)
        editor.remove(KEY_SAVED_ACCOUNT)
        editor.remove(KEY_ACCOUNT_TYPE)
        editor.apply()
    }
    
    /**
     * 验证账号和密码
     * 
     * @param account 账号字符串
     * @param password 密码字符串
     * @return 验证结果和错误信息
     */
    fun validateLoginInput(account: String, password: String): Pair<Boolean, String> {
        return when {
            account.isEmpty() -> Pair(false, "请输入账号")
            password.isEmpty() -> Pair(false, "请输入密码")
            !AccountTypeDetector.isValidAccount(account) -> {
                Pair(false, "账号格式不正确，请输入手机号、邮箱或用户名")
            }
            password.length < 6 -> Pair(false, "密码长度不能少于 6 位")
            else -> Pair(true, "")
        }
    }
}
