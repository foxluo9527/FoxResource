package com.foxluo.baselib.util

import android.widget.ImageView
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.bumptech.glide.Glide
import com.foxluo.baselib.R
import com.foxluo.baselib.data.respository.BASE_URL
import com.foxluo.baselib.ui.BaseApplication
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.CropCircleTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

object ImageExt {
    fun ImageView.loadUrl(url: String?) {
        Glide.with(this)
            .load(processUrl(url)?:R.mipmap.ic_app)
            .placeholder(R.mipmap.ic_app)
            .error(R.mipmap.ic_app)
            .into(this)
    }

    fun ImageView.loadUrlWithBlur(url: String?) {
        Glide.with(this)
            .load(processUrl(url)?:R.mipmap.ic_app)
            .transform(BlurTransformation(100))
            .placeholder(R.mipmap.ic_app_blur)
            .error(R.mipmap.ic_app_blur)
            .into(this)
    }

    fun ImageView.loadUrlWithCorner(url: String?, radius: Int) {
        Glide.with(this)
            .load(processUrl(url)?:R.mipmap.ic_app)
            .transform(RoundedCornersTransformation(dp2px(radius.toFloat()), 0))
            .placeholder(R.mipmap.ic_app)
            .error(R.mipmap.ic_app)
            .into(this)
    }

    fun ImageView.loadUrlWithCircle(url: String?) {
        Glide.with(this)
            .load(processUrl(url)?:R.mipmap.ic_app)
            .transform(CropCircleTransformation())
            .placeholder(R.mipmap.ic_app_round)
            .error(R.mipmap.ic_app_round)
            .into(this)
    }

    fun processUrl(url: String?): String? {
        return if (!(url.isNullOrEmpty()) && url.startsWith("http") == false) {
            BASE_URL + url
        } else {
            if (url.isNullOrEmpty()) {
                null
            } else {
                url
            }
        }
    }
}