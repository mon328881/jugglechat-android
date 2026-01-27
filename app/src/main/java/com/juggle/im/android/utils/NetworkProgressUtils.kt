package com.juggle.im.android.utils

import android.content.Context
import android.util.Log
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 网络加载进度工具类
 * 用于显示网络加载进度和允许用户取消加载
 */
object NetworkProgressUtils {

    private const val TAG = "NetworkProgressUtils"
    private const val BUFFER_SIZE = 8192

    /**
     * 网络加载进度回调接口
     */
    interface ProgressListener {
        /**
         * 加载进度更新
         * @param bytesRead 已读取字节数
         * @param totalBytes 总字节数
         * @param progress 进度百分比（0-100）
         */
        fun onProgress(bytesRead: Long, totalBytes: Long, progress: Int)

        /**
         * 加载完成
         * @param data 加载的数据
         */
        fun onComplete(data: ByteArray)

        /**
         * 加载失败
         * @param error 错误信息
         */
        fun onError(error: String)

        /**
         * 加载取消
         */
        fun onCancel()
    }

    /**
     * 下载文件并显示进度
     * @param url 文件 URL
     * @param listener 进度监听器
     * @return 是否成功下载
     */
    fun downloadWithProgress(
        url: String,
        listener: ProgressListener
    ): Boolean {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val totalBytes = connection.contentLength.toLong()
            val inputStream = connection.inputStream

            val buffer = ByteArray(BUFFER_SIZE)
            val outputBuffer = mutableListOf<ByteArray>()
            var bytesRead: Long = 0
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputBuffer.add(buffer.copyOf(read))
                bytesRead += read

                val progress = if (totalBytes > 0) {
                    ((bytesRead * 100) / totalBytes).toInt()
                } else {
                    0
                }

                listener.onProgress(bytesRead, totalBytes, progress)
            }

            inputStream.close()
            connection.disconnect()

            // 合并所有字节
            val totalData = ByteArray(bytesRead.toInt())
            var offset = 0
            for (chunk in outputBuffer) {
                System.arraycopy(chunk, 0, totalData, offset, chunk.size)
                offset += chunk.size
            }

            listener.onComplete(totalData)
            true
        } catch (e: Exception) {
            Log.e(TAG, "下载失败: $url", e)
            listener.onError(e.message ?: "未知错误")
            false
        }
    }

    /**
     * 下载文件到本地并显示进度
     * @param url 文件 URL
     * @param outputPath 输出文件路径
     * @param listener 进度监听器
     * @return 是否成功下载
     */
    fun downloadFileWithProgress(
        url: String,
        outputPath: String,
        listener: ProgressListener
    ): Boolean {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val totalBytes = connection.contentLength.toLong()
            val inputStream = connection.inputStream
            val outputFile = java.io.File(outputPath)
            val outputStream = java.io.FileOutputStream(outputFile)

            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Long = 0
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                bytesRead += read

                val progress = if (totalBytes > 0) {
                    ((bytesRead * 100) / totalBytes).toInt()
                } else {
                    0
                }

                listener.onProgress(bytesRead, totalBytes, progress)
            }

            outputStream.close()
            inputStream.close()
            connection.disconnect()

            listener.onComplete(ByteArray(0))
            true
        } catch (e: Exception) {
            Log.e(TAG, "下载文件失败: $url", e)
            listener.onError(e.message ?: "未知错误")
            false
        }
    }

    /**
     * 获取网络连接速度
     * @param url 测试 URL
     * @return 连接速度（字节/秒），如果获取失败返回 0
     */
    fun getNetworkSpeed(url: String): Long {
        return try {
            val startTime = System.currentTimeMillis()
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val contentLength = connection.contentLength.toLong()
            connection.disconnect()

            val endTime = System.currentTimeMillis()
            val duration = (endTime - startTime) / 1000 // 转换为秒

            if (duration > 0) contentLength / duration else 0L
        } catch (e: Exception) {
            Log.e(TAG, "获取网络速度失败", e)
            0L
        }
    }

    /**
     * 检查网络连接
     * @param context 上下文
     * @return 如果网络连接正常返回 true，否则返回 false
     */
    fun isNetworkConnected(context: Context): Boolean {
        return try {
            val connection = URL("http://www.google.com").openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e(TAG, "网络连接检查失败", e)
            false
        }
    }

    /**
     * 格式化网络速度
     * @param bytesPerSecond 字节/秒
     * @return 格式化后的速度字符串
     */
    fun formatNetworkSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
            bytesPerSecond < 1024 * 1024 -> "${bytesPerSecond / 1024} KB/s"
            else -> "${bytesPerSecond / (1024 * 1024)} MB/s"
        }
    }
}
