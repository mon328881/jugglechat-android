package com.juggle.im.android.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.juggle.im.android.model.LocationData
import com.juggle.im.android.theme.ThemeManager
import com.juggle.im.android.utils.LocationPermissionUtils

/**
 * 位置选择 Activity
 * 允许用户选择位置并分享
 * 验证: 需求 16.1, 16.5
 */
class LocationPickerActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_LOCATION = "location"
        const val REQUEST_LOCATION_PERMISSION = 100
    }
    
    private lateinit var themeManager: ThemeManager
    private lateinit var theme: com.juggle.im.android.theme.ThemeConfig
    
    private lateinit var locationNameInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var latitudeInput: EditText
    private lateinit var longitudeInput: EditText
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化主题
        themeManager = ThemeManager.getInstance(this)
        theme = themeManager.getCurrentTheme()
        
        // 创建布局
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(theme.spacing.md, theme.spacing.md, theme.spacing.md, theme.spacing.md)
            setBackgroundColor(theme.colors.background)
        }
        
        // 位置名称输入框
        locationNameInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.md)
            }
            hint = "位置名称"
            setTextColor(theme.colors.onBackground)
            setHintTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
        }
        mainLayout.addView(locationNameInput)
        
        // 地址输入框
        addressInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.md)
            }
            hint = "地址"
            setTextColor(theme.colors.onBackground)
            setHintTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
        }
        mainLayout.addView(addressInput)
        
        // 纬度输入框
        latitudeInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.md)
            }
            hint = "纬度"
            setTextColor(theme.colors.onBackground)
            setHintTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        mainLayout.addView(latitudeInput)
        
        // 经度输入框
        longitudeInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, theme.spacing.md)
            }
            hint = "经度"
            setTextColor(theme.colors.onBackground)
            setHintTextColor(theme.colors.onBackground.let { 
                (it and 0xFFFFFF) or 0x80000000.toInt()
            })
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        mainLayout.addView(longitudeInput)
        
        // 按钮容器
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, theme.spacing.md, 0, 0)
            }
        }
        
        // 取消按钮
        cancelButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, theme.spacing.sm, 0)
            }
            text = "取消"
            setOnClickListener {
                finish()
            }
        }
        buttonContainer.addView(cancelButton)
        
        // 确认按钮
        confirmButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            text = "确认"
            setOnClickListener {
                confirmLocation()
            }
        }
        buttonContainer.addView(confirmButton)
        
        mainLayout.addView(buttonContainer)
        
        setContentView(mainLayout)
        
        // 检查位置权限
        checkLocationPermission()
    }
    
    /**
     * 检查位置权限
     */
    private fun checkLocationPermission() {
        if (LocationPermissionUtils.shouldRequestPermission(this)) {
            val permissions = LocationPermissionUtils.getPermissionsToRequest(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }
    
    /**
     * 处理权限请求结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
                Toast.makeText(this, "位置权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                // 权限被拒绝
                Toast.makeText(this, "位置权限被拒绝，请手动输入位置信息", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 确认位置选择
     */
    private fun confirmLocation() {
        val locationName = locationNameInput.text.toString().trim()
        val address = addressInput.text.toString().trim()
        val latitudeStr = latitudeInput.text.toString().trim()
        val longitudeStr = longitudeInput.text.toString().trim()
        
        // 验证输入
        if (locationName.isEmpty()) {
            Toast.makeText(this, "请输入位置名称", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            Toast.makeText(this, "请输入纬度和经度", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val latitude = latitudeStr.toDouble()
            val longitude = longitudeStr.toDouble()
            
            // 验证坐标范围
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
                Toast.makeText(this, "坐标范围无效", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 创建位置数据
            val location = LocationData(
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                address = address
            )
            
            // 返回结果
            val intent = Intent().apply {
                putExtra(EXTRA_LOCATION, location)
            }
            setResult(RESULT_OK, intent)
            finish()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "坐标格式无效", Toast.LENGTH_SHORT).show()
        }
    }
}
