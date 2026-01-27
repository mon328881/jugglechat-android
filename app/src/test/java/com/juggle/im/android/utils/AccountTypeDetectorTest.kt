package com.juggle.im.android.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * 账号类型检测单元测试
 * 验证账号类型自动识别功能
 */
class AccountTypeDetectorTest {
    
    /**
     * 属性 5: 登录账号类型支持
     * 验证: 需求 2.2, 2.3
     * 
     * 登录界面应该支持三种账号类型：字母+数字、手机号、邮箱，
     * 并能自动识别账号类型
     */
    @Test
    fun testAccountTypeDetection() {
        // 测试手机号识别
        val phoneAccount = "13800138000"
        assertEquals("应该识别为手机号", 
            AccountTypeDetector.AccountType.PHONE, 
            AccountTypeDetector.detectAccountType(phoneAccount))
        
        // 测试邮箱识别
        val emailAccount = "user@example.com"
        assertEquals("应该识别为邮箱", 
            AccountTypeDetector.AccountType.EMAIL, 
            AccountTypeDetector.detectAccountType(emailAccount))
        
        // 测试字母+数字识别
        val alphanumericAccount = "user123"
        assertEquals("应该识别为字母+数字", 
            AccountTypeDetector.AccountType.ALPHANUMERIC, 
            AccountTypeDetector.detectAccountType(alphanumericAccount))
    }
    
    /**
     * 验证手机号识别
     */
    @Test
    fun testPhoneNumberDetection() {
        // 有效的手机号
        assertTrue("13800138000 应该被识别为手机号", 
            AccountTypeDetector.detectAccountType("13800138000") == AccountTypeDetector.AccountType.PHONE)
        assertTrue("15900000000 应该被识别为手机号", 
            AccountTypeDetector.detectAccountType("15900000000") == AccountTypeDetector.AccountType.PHONE)
        
        // 无效的手机号
        assertFalse("12800138000 不应该被识别为手机号（以1开头但第二位不对）", 
            AccountTypeDetector.detectAccountType("12800138000") == AccountTypeDetector.AccountType.PHONE)
        assertFalse("1380013800 不应该被识别为手机号（位数不足）", 
            AccountTypeDetector.detectAccountType("1380013800") == AccountTypeDetector.AccountType.PHONE)
    }
    
    /**
     * 验证邮箱识别
     */
    @Test
    fun testEmailDetection() {
        // 有效的邮箱
        assertTrue("user@example.com 应该被识别为邮箱", 
            AccountTypeDetector.detectAccountType("user@example.com") == AccountTypeDetector.AccountType.EMAIL)
        assertTrue("test.user@domain.co.uk 应该被识别为邮箱", 
            AccountTypeDetector.detectAccountType("test.user@domain.co.uk") == AccountTypeDetector.AccountType.EMAIL)
        
        // 无效的邮箱
        assertFalse("user@example 不应该被识别为邮箱（缺少顶级域名）", 
            AccountTypeDetector.detectAccountType("user@example") == AccountTypeDetector.AccountType.EMAIL)
        assertFalse("userexample.com 不应该被识别为邮箱（缺少@符号）", 
            AccountTypeDetector.detectAccountType("userexample.com") == AccountTypeDetector.AccountType.EMAIL)
    }
    
    /**
     * 验证字母+数字识别
     */
    @Test
    fun testAlphanumericDetection() {
        // 有效的字母+数字
        assertTrue("user123 应该被识别为字母+数字", 
            AccountTypeDetector.detectAccountType("user123") == AccountTypeDetector.AccountType.ALPHANUMERIC)
        assertTrue("abc123def 应该被识别为字母+数字", 
            AccountTypeDetector.detectAccountType("abc123def") == AccountTypeDetector.AccountType.ALPHANUMERIC)
        
        // 无效的字母+数字
        assertFalse("user 不应该被识别为字母+数字（长度不足）", 
            AccountTypeDetector.detectAccountType("user") == AccountTypeDetector.AccountType.ALPHANUMERIC)
        assertFalse("user_123 不应该被识别为字母+数字（包含特殊字符）", 
            AccountTypeDetector.detectAccountType("user_123") == AccountTypeDetector.AccountType.ALPHANUMERIC)
    }
    
    /**
     * 验证账号有效性检查
     */
    @Test
    fun testAccountValidation() {
        // 有效账号
        assertTrue("13800138000 应该是有效账号", 
            AccountTypeDetector.isValidAccount("13800138000"))
        assertTrue("user@example.com 应该是有效账号", 
            AccountTypeDetector.isValidAccount("user@example.com"))
        assertTrue("user123 应该是有效账号", 
            AccountTypeDetector.isValidAccount("user123"))
        
        // 无效账号
        assertFalse("abc 不应该是有效账号", 
            AccountTypeDetector.isValidAccount("abc"))
        assertFalse("123 不应该是有效账号", 
            AccountTypeDetector.isValidAccount("123"))
        assertFalse("user@invalid 不应该是有效账号", 
            AccountTypeDetector.isValidAccount("user@invalid"))
    }
    
    /**
     * 验证账号类型显示名称
     */
    @Test
    fun testAccountTypeDisplayName() {
        assertEquals("手机号类型应该显示为'手机号'", 
            "手机号", 
            AccountTypeDetector.getAccountTypeDisplayName(AccountTypeDetector.AccountType.PHONE))
        assertEquals("邮箱类型应该显示为'邮箱'", 
            "邮箱", 
            AccountTypeDetector.getAccountTypeDisplayName(AccountTypeDetector.AccountType.EMAIL))
        assertEquals("字母+数字类型应该显示为'用户名'", 
            "用户名", 
            AccountTypeDetector.getAccountTypeDisplayName(AccountTypeDetector.AccountType.ALPHANUMERIC))
    }
}
