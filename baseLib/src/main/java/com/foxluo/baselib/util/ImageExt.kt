package com.foxluo.baselib.util

import android.widget.ImageView
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ImageExt.loadUrl
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.CropCircleTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

object ImageExt {
    fun ImageView.loadUrl(url:String){
        Glide.with(this)
            .load(url)
            .placeholder(R.mipmap.ic_app)
            .into(this)
    }

    fun ImageView.loadUrlWithBlur(url:String){
        Glide.with(this)
            .load(url)
            .transform(BlurTransformation(15))
            .placeholder(R.mipmap.ic_app)
            .into(this)
    }

    fun ImageView.loadUrlWithCorner(url:String,radius:Int){
        Glide.with(this)
            .load(url)
            .transform(RoundedCornersTransformation(dp2px(radius.toFloat()),0))
            .placeholder(R.mipmap.ic_app)
            .into(this)
    }

    fun ImageView.loadUrlWithCircle(url:String){
        Glide.with(this)
            .load(url)
            .transform(CropCircleTransformation())
            .placeholder(R.mipmap.ic_app)
            .into(this)
    }
}