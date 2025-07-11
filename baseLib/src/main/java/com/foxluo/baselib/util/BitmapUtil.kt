package com.foxluo.baselib.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ImageExt.processUrl
import jp.wasabeef.glide.transformations.BitmapTransformation
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BitmapUtil {
    interface ColorFilterCallback {
        suspend fun onColorFilterChanged(primaryColor: Int, primaryTextColor: Int, secondaryTextColor: Int)
    }

    suspend fun Context.getImageColors(
        url: String?,
        playCallback:ColorFilterCallback
    ) {
        withContext(Dispatchers.IO) {
            val bitmap =
                runCatching {
                    Glide.with(this@getImageColors)
                        .asBitmap()
                        .load(processUrl(url) ?: R.mipmap.ic_app)
                        .transform(BlurTransformation(100))
                        .override(150)
                        .placeholder(R.mipmap.ic_app_blur)
                        .error(R.mipmap.ic_app_blur)
                        .submit()
                        .get()
                }.getOrNull() ?: BitmapFactory.decodeResource(resources, R.mipmap.ic_app)
            val palette = Palette.from(bitmap).generate()
            val swatch = palette.swatches.firstOrNull() { it != null }
            swatch?.let {
                playCallback.onColorFilterChanged(it.rgb, it.bodyTextColor, it.titleTextColor)
            }
        }
    }
}