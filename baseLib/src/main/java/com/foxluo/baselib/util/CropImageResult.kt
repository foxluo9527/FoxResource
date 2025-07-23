package com.foxluo.baselib.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.Utils
import com.foxluo.baselib.ui.CropActivity
import com.foxluo.baselib.util.StringUtil.prefix
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 裁剪图片配置类
 */
class CropImageResult(
    var uri: Uri,
    val aspectX: Float = 1f, //裁剪框横向比例数值
    val aspectY: Float = 1f//裁剪框纵向比例数值
)

suspend fun Uri.getFilePath(cropped: Boolean): String? {
    //contentProvider会提供一个当前activity可以读取的uri，离开页面后会作废，使用takePersistableUriPermission让应用长时间持有文件访问权限
    //applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return if (!cropped) {
        withContext(Dispatchers.IO) {
            Utils.getApp().applicationContext.contentResolver.query(
                this@getFilePath, null, null, null, null, null
            )?.let { cursor ->
                try {
                    cursor.moveToFirst()
                    val dataColIndex = cursor.getColumnIndex("_data")
                    //使用contentResolver查询临时uri的文件路径，这个filePath只能用于上传，退出应用后将无法再访问文件
                    val dataPath: String = cursor.getString(dataColIndex)
                    dataPath
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    cursor.close()
                }
            }
        }
    } else {
        pathSegments?.last()?.prefix(Utils.getApp().cacheDir.path + "/")
    }
}

/**
 * 裁剪图片
 */
class CropImageContract : ActivityResultContract<CropImageResult, Uri?>() {

    override fun createIntent(context: Context, input: CropImageResult): Intent {
        // 在应用私有目录创建临时文件
        val outputFile = File(
            context.cacheDir,
            "cropped_${System.currentTimeMillis()}.jpg"
        )

        // 使用FileProvider生成安全URI
        val mUriOutput = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // 与manifest配置一致
            outputFile
        )
        val ucrop = UCrop.of(input.uri, mUriOutput!!)
        if (input.aspectX != 0f && input.aspectY != 0f) {
            ucrop.withAspectRatio(input.aspectX, input.aspectY)
        }
        val intent = ucrop.getIntent(context).apply {
            setClass(context, CropActivity::class.java)
        }
        return intent
    }

    override fun parseResult(resultCode: Int, data: Intent?): Uri? {
        val resultUri = if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            UCrop.getOutput(data)
        } else null
        return resultUri
    }
}