package com.juggle.im.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

/**
 * 图片压缩工具类
 * 用于自动压缩上传的图片
 */
object ImageCompressionUtils {

    private const val TAG = "ImageCompressionUtils"
    private const val MAX_WIDTH = 1920
    private const val MAX_HEIGHT = 1920
    private const val QUALITY_HIGH = 85
    private const val QUALITY_MEDIUM = 75
    private const val QUALITY_LOW = 65
    private const val MAX_FILE_SIZE = 500 * 1024 // 500KB

    /**
     * 压缩图片文件
     * @param context 上下文
     * @param imageUri 图片 URI
     * @param outputFile 输出文件
     * @return 压缩后的文件，如果压缩失败返回 null
     */
    fun compressImage(
        context: Context,
        imageUri: Uri,
        outputFile: File
    ): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                Log.e(TAG, "无法解码图片")
                return null
            }

            // 缩放图片
            val scaledBitmap = scaleBitmap(bitmap)

            // 压缩并保存
            compressAndSaveBitmap(scaledBitmap, outputFile)

            scaledBitmap.recycle()
            bitmap.recycle()

            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "图片压缩失败", e)
            null
        }
    }

    /**
     * 压缩图片文件（从文件路径）
     * @param imagePath 图片文件路径
     * @param outputFile 输出文件
     * @return 压缩后的文件，如果压缩失败返回 null
     */
    fun compressImageFromPath(
        imagePath: String,
        outputFile: File
    ): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap == null) {
                Log.e(TAG, "无法解码图片: $imagePath")
                return null
            }

            // 缩放图片
            val scaledBitmap = scaleBitmap(bitmap)

            // 压缩并保存
            compressAndSaveBitmap(scaledBitmap, outputFile)

            scaledBitmap.recycle()
            bitmap.recycle()

            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "图片压缩失败", e)
            null
        }
    }

    /**
     * 缩放图片
     * @param bitmap 原始 Bitmap
     * @return 缩放后的 Bitmap
     */
    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // 如果图片尺寸已经满足要求，直接返回
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return bitmap
        }

        // 计算缩放比例
        val scale = minOf(
            MAX_WIDTH.toFloat() / width,
            MAX_HEIGHT.toFloat() / height
        )

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * 压缩并保存 Bitmap
     * @param bitmap Bitmap
     * @param outputFile 输出文件
     */
    private fun compressAndSaveBitmap(bitmap: Bitmap, outputFile: File) {
        var quality = QUALITY_HIGH
        var compressedData: ByteArray

        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedData = outputStream.toByteArray()
            outputStream.close()

            quality -= 5
        } while (compressedData.size > MAX_FILE_SIZE && quality > 0)

        // 保存到文件
        val fileOutputStream = FileOutputStream(outputFile)
        fileOutputStream.write(compressedData)
        fileOutputStream.close()

        Log.d(TAG, "图片压缩完成: ${compressedData.size} bytes, 质量: $quality")
    }

    /**
     * 获取图片尺寸
     * @param imagePath 图片文件路径
     * @return 图片尺寸 (宽, 高)
     */
    fun getImageDimensions(imagePath: String): Pair<Int, Int>? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)
            Pair(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            Log.e(TAG, "获取图片尺寸失败", e)
            null
        }
    }

    /**
     * 获取图片文件大小
     * @param file 图片文件
     * @return 文件大小（字节）
     */
    fun getImageFileSize(file: File): Long {
        return if (file.exists()) file.length() else 0L
    }

    /**
     * 格式化文件大小
     * @param bytes 字节数
     * @return 格式化后的文件大小字符串
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    /**
     * 计算图片相似度（用于去重）
     * @param bitmap1 第一张图片
     * @param bitmap2 第二张图片
     * @return 相似度（0-1，1 表示完全相同）
     */
    fun calculateImageSimilarity(bitmap1: Bitmap, bitmap2: Bitmap): Float {
        if (bitmap1.width != bitmap2.width || bitmap1.height != bitmap2.height) {
            return 0f
        }

        var diffPixels = 0
        val totalPixels = bitmap1.width * bitmap1.height

        for (y in 0 until bitmap1.height) {
            for (x in 0 until bitmap1.width) {
                if (bitmap1.getPixel(x, y) != bitmap2.getPixel(x, y)) {
                    diffPixels++
                }
            }
        }

        return 1f - (diffPixels.toFloat() / totalPixels)
    }
}
