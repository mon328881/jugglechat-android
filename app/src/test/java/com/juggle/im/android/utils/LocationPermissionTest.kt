package com.juggle.im.android.utils

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 位置权限工具单元测试
 * 验证: 需求 16.5
 */
class LocationPermissionTest {
    
    private lateinit var context: Context
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }
    
    @Test
    fun testLocationPermissionsArray() {
        assertEquals(2, LocationPermissionUtils.LOCATION_PERMISSIONS.size)
        assertTrue(LocationPermissionUtils.LOCATION_PERMISSIONS.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(LocationPermissionUtils.LOCATION_PERMISSIONS.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    
    @Test
    fun testHasLocationPermission() {
        // 这个测试的结果取决于运行时权限状态
        val hasPermission = LocationPermissionUtils.hasLocationPermission(context)
        assertNotNull(hasPermission)
    }
    
    @Test
    fun testShouldRequestPermission() {
        val shouldRequest = LocationPermissionUtils.shouldRequestPermission(context)
        assertNotNull(shouldRequest)
    }
    
    @Test
    fun testGetPermissionsToRequest() {
        val permissions = LocationPermissionUtils.getPermissionsToRequest(context)
        assertNotNull(permissions)
        assertTrue(permissions is Array<*>)
    }
    
    @Test
    fun testPermissionConsistency() {
        val hasPermission = LocationPermissionUtils.hasLocationPermission(context)
        val shouldRequest = LocationPermissionUtils.shouldRequestPermission(context)
        
        // 如果有权限，就不应该请求；反之亦然
        assertEquals(hasPermission, !shouldRequest)
    }
    
    @Test
    fun testPermissionsToRequestAreValid() {
        val permissions = LocationPermissionUtils.getPermissionsToRequest(context)
        
        // 所有返回的权限都应该在权限列表中
        permissions.forEach { permission ->
            assertTrue(LocationPermissionUtils.LOCATION_PERMISSIONS.contains(permission))
        }
    }
}
