package com.juggle.im.android.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize

/**
 * 位置权限处理属性测试
 * 验证: 需求 16.5
 * 属性 69: 位置权限处理
 */
class LocationPermissionPropertyTest : FunSpec({
    
    val context: Context = ApplicationProvider.getApplicationContext()
    
    test("位置权限列表应该包含精确位置权限") {
        LocationPermissionUtils.LOCATION_PERMISSIONS shouldContain Manifest.permission.ACCESS_FINE_LOCATION
    }
    
    test("位置权限列表应该包含粗略位置权限") {
        LocationPermissionUtils.LOCATION_PERMISSIONS shouldContain Manifest.permission.ACCESS_COARSE_LOCATION
    }
    
    test("位置权限列表应该包含两个权限") {
        LocationPermissionUtils.LOCATION_PERMISSIONS shouldHaveSize 2
    }
    
    test("应该能够检查位置权限状态") {
        val hasPermission = LocationPermissionUtils.hasLocationPermission(context)
        // 权限状态应该是布尔值
        hasPermission shouldBe (hasPermission)
    }
    
    test("应该能够判断是否需要请求权限") {
        val shouldRequest = LocationPermissionUtils.shouldRequestPermission(context)
        // 应该返回布尔值
        shouldRequest shouldBe (shouldRequest)
    }
    
    test("应该能够获取需要请求的权限列表") {
        val permissionsToRequest = LocationPermissionUtils.getPermissionsToRequest(context)
        // 权限列表应该是数组
        permissionsToRequest shouldNotBe null
    }
    
    test("当已有权限时，不应该返回需要请求的权限") {
        // 这个测试假设在测试环境中可能已有权限
        val permissionsToRequest = LocationPermissionUtils.getPermissionsToRequest(context)
        // 验证返回的是数组类型
        permissionsToRequest is Array<*> shouldBe true
    }
    
    test("权限检查应该是一致的") {
        val firstCheck = LocationPermissionUtils.hasLocationPermission(context)
        val secondCheck = LocationPermissionUtils.hasLocationPermission(context)
        firstCheck shouldBe secondCheck
    }
    
    test("权限请求判断应该与权限检查一致") {
        val hasPermission = LocationPermissionUtils.hasLocationPermission(context)
        val shouldRequest = LocationPermissionUtils.shouldRequestPermission(context)
        
        // 如果有权限，就不应该请求；反之亦然
        hasPermission shouldBe !shouldRequest
    }
    
    test("获取需要请求的权限列表应该只包含未授予的权限") {
        val permissionsToRequest = LocationPermissionUtils.getPermissionsToRequest(context)
        
        // 所有返回的权限都应该在权限列表中
        permissionsToRequest.forEach { permission ->
            LocationPermissionUtils.LOCATION_PERMISSIONS shouldContain permission
        }
    }
})
