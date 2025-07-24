package com.foxluo.baselib.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.Utils
import com.foxluo.baselib.ui.CropActivity
import com.foxluo.baselib.util.StringUtil.prefix
import com.foxluo.baselib.util.TimeUtil.nowTime
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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

suspend fun Uri.getFilePath(isInternal: Boolean = this.toString().contains(Utils.getApp().packageName)): String? {
    //contentProvider会提供一个当前activity可以读取的uri，离开页面后会作废，使用takePersistableUriPermission让应用长时间持有文件访问权限
    //applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return if (!isInternal) {
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
 * 获取输出文件的uri
 * @param path 完整地址
 * @param prefix 时间戳文件名的前缀
 * @param suffix 时间戳文件名的后缀
 */
fun Context.getOutPutUri(path: String? = null, prefix: String, suffix: String): Uri {
    val context = this
    // 在应用私有目录创建临时文件
    val outputFile = File(
        context.cacheDir,
        path ?: "${prefix}${nowTime}${suffix}"
    )

    // 使用FileProvider生成安全URI
    val mUriOutput = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider", // 与manifest配置一致
        outputFile
    )

    return mUriOutput
}

/**
 * 裁剪图片
 */
class CropImageContract : ActivityResultContract<CropImageResult, Uri?>() {

    override fun createIntent(context: Context, input: CropImageResult): Intent {
        val inputFilePath = runBlocking { input.uri.getFilePath() }?.split("/")?.last()
        val outputUri = context.getOutPutUri("cropped_" + inputFilePath, "", ".jpg")
        val ucrop = UCrop.of(input.uri, outputUri)
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