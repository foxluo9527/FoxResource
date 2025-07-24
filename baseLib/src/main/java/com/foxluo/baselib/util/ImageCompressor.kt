package com.foxluo.baselib.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ImageUtils.calculateInSampleSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageCompressor {

    // 压缩图片
    suspend fun compressImage(
        sourcePath: String = "",
        sourceFile: File = File(sourcePath),
        targetPath: String = "",
        targetFile: File = File(targetPath),
        maxWidth: Int = 1080,
        maxHeight: Int = 1920
    ) {
        withContext(Dispatchers.IO) {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            // 计算采样率
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565

            var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options) ?: return@withContext
            val originalWidth = bitmap.width.toFloat()
            val originalHeight = bitmap.height.toFloat()
            val aspectRatio = originalWidth / originalHeight

            // 计算两种约束条件下的目标尺寸
            val widthBasedHeight = maxWidth / aspectRatio
            val heightBasedWidth = maxHeight * aspectRatio

            // 选择符合双约束的尺寸
            val targetWidth = when {
                heightBasedWidth <= maxWidth -> heightBasedWidth.toInt()
                else -> maxWidth
            }

            val targetHeight = when {
                heightBasedWidth <= maxWidth -> maxHeight
                else -> widthBasedHeight.toInt()
            }
            // 处理图片旋转
            bitmap = rotateBitmapIfRequired(bitmap, sourceFile)

            // 尺寸压缩
            val scaledBitmap = ImageUtils.scale(bitmap, targetWidth, targetHeight)

            // 质量压缩
            ImageUtils.save(scaledBitmap, targetFile, Bitmap.CompressFormat.WEBP)

            bitmap.recycle()
            scaledBitmap.recycle()
        }
    }

    private fun rotateBitmapIfRequired(bitmap: Bitmap, file: File): Bitmap {
        val exif = ExifInterface(file)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degree) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}