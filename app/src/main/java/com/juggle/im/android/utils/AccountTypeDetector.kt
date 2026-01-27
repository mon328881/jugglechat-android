package com.juggle.im.android.utils

/**
 * 账号类型检测工具
 * 用于自动识别账号类型（字母+数字、手机号、邮箱）
 */
object AccountTypeDetector {
    
    /**
     * 账号类型枚举
     */
    enum class AccountType {
        ALPHANUMERIC,  // 字母+数字
        PHONE,         // 手机号
        EMAIL,         // 邮箱
        UNKNOWN        // 未知
    }
    
    /**
     * 检测账号类型
     * 
     * @param account 账号字符串
     * @return 账号类型
     */
    fun detectAccountType(account: String): AccountType {
        return when {
            isPhoneNumber(account) -> AccountType.PHONE
            isEmail(account) -> AccountType.EMAIL
            isAlphanumeric(account) -> AccountType.ALPHANUMERIC
            else -> AccountType.UNKNOWN
        }
    }
    
    /**
     * 检查是否为手机号
     * 支持中国手机号格式（11位数字，以1开头）
     * 
     * @param account 账号字符串
     * @return 是否为手机号
     */
    private fun isPhoneNumber(account: String): Boolean {
        // 中国手机号：11位数字，以1开头
        val phoneRegex = "^1[3-9]\\d{9}$".toRegex()
        return phoneRegex.matches(account)
    }
    
    /**
     * 检查是否为邮箱
     * 
     * @param account 账号字符串
     * @return 是否为邮箱
     */
    private fun isEmail(account: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(account)
    }
    
    /**
     * 检查是否为字母+数字组合
     * 
     * @param account 账号字符串
     * @return 是否为字母+数字组合
     */
    private fun isAlphanumeric(account: String): Boolean {
        // 字母+数字，长度 4-20
        val alphanumericRegex = "^[A-Za-z0-9]{4,20}$".toRegex()
        return alphanumericRegex.matches(account)
    }
    
    /**
     * 获取账号类型的显示名称
     * 
     * @param type 账号类型
     * @return 显示名称
     */
    fun getAccountTypeDisplayName(type: AccountType): String {
        return when (type) {
            AccountType.PHONE -> "手机号"
            AccountType.EMAIL -> "邮箱"
            AccountType.ALPHANUMERIC -> "用户名"
            AccountType.UNKNOWN -> "未知"
        }
    }
    
    /**
     * 验证账号格式
     * 
     * @param account 账号字符串
     * @return 是否为有效账号
     */
    fun isValidAccount(account: String): Boolean {
        return detectAccountType(account) != AccountType.UNKNOWN
    }
}
