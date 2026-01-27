package com.juggle.im.android.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * 位置权限管理工具类
 * 处理位置权限的请求和检查
 * 验证: 需求 16.5
 */
object LocationPermissionUtils {
    
    // 位置权限列表
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    /**
     * 检查是否已获得位置权限
     */
    fun hasLocationPermission(context: Context): Boolean {
        return LOCATION_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 检查是否需要请求权限
     */
    fun shouldRequestPermission(context: Context): Boolean {
        return !hasLocationPermission(context)
    }
    
    /**
     * 获取需要请求的权限列表
     */
    fun getPermissionsToRequest(context: Context): Array<String> {
        return LOCATION_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
}
