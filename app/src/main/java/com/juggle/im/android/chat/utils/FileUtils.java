package com.juggle.im.android.chat.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 文件工具类：处理文件转换和操作
 */
public class FileUtils {
    /**
     * 将ContentUri转换为本地文件路径
     *
     * @param context    应用上下文
     * @param contentUri ContentUri字符串
     * @return 本地文件路径，如果转换失败返回null
     */
    public static String convertContentUriToFile(Context context, String contentUri) {
        return convertContentUriToFile(context, contentUri, "temp_image.jpg");
    }

    /**
     * 将content://Uri拷贝到缓存目录，并按照给定后缀生成临时文件
     *
     * @param context    上下文
     * @param contentUri content://形式的Uri字符串
     * @param suffix     临时文件名后缀，比如"temp_image.jpg"、"temp_video.mp4"
     * @return 临时文件的绝对路径
     */
    public static String convertContentUriToFile(Context context, String contentUri, String suffix) {
        if (!contentUri.startsWith("content://")) {
            return contentUri;
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File tempFile = null;

        try {
            // 获取ContentResolver
            ContentResolver resolver = context.getContentResolver();

            // 获取输入流
            inputStream = resolver.openInputStream(Uri.parse(contentUri));

            // 创建临时文件
            tempFile = new File(context.getCacheDir(), System.currentTimeMillis() + suffix);
            if (tempFile.exists()) {
                tempFile.delete(); // 如果文件存在，删除它
            }

            // 写入文件
            outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            Log.e("FileUtils", "转换ContentUri到文件时出错", e);
        } finally {
            // 关闭流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                Log.e("FileUtils", "关闭流时出错", e);
            }
        }
        return tempFile != null ? tempFile.getAbsolutePath() : null;
    }

    /**
     * 删除临时文件
     */
    public static boolean deleteTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            return tempFile.delete();
        }
        return false;
    }

    /**
     * 复制文件到收藏目录，用于持久化收藏的图片/文件
     *
     * @param context       上下文
     * @param srcPath       源文件路径
     * @param destFilename  目标文件名（含扩展名）
     * @return 目标文件绝对路径，失败返回null
     */
    public static String copyToFavoritesDir(Context context, String srcPath, String destFilename) {
        if (srcPath == null || srcPath.isEmpty()) return null;
        File src = new File(srcPath);
        if (!src.exists()) return null;
        File dir = new File(context.getFilesDir(), "favorites");
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(dir, destFilename);
        try (InputStream in = new java.io.FileInputStream(src);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.flush();
            return dest.getAbsolutePath();
        } catch (Exception e) {
            Log.e("FileUtils", "复制到收藏目录时出错", e);
            return null;
        }
    }

    /**
     * 创建临时图片文件
     *
     * @param context 上下文
     * @return 临时文件的Uri
     */
    public static Uri createTmpImageFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getCacheDir();
        try {
            File photoFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            String authority = context.getPackageName() + ".fileprovider";
            Uri photoURI = FileProvider.getUriForFile(context, authority, photoFile);
            return photoURI;
        } catch (Exception e) {
            Log.e("FileUtils", "创建临时文件时出错", e);
            throw new RuntimeException(e);
        }
    }
}
