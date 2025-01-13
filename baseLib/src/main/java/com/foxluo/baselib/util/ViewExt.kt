package com.foxluo.baselib.util

import android.view.View

object ViewExt {
    //扩展函数，view隐藏
    fun View.gone() {
        visibility = View.GONE
    }

    //扩展函数，view显示
    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.visible(visible: Boolean) {
        visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}