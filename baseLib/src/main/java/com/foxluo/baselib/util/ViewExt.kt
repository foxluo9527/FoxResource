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

    /**
     * 防止连续点击
     */
    fun View.fastClick(clickDelay: Long = 1000L, callback: (View) -> Unit) {
        setOnClickListener {
            val currentTime = System.currentTimeMillis()
            ((getTag(id) as? Long)?.let {
                if (it + clickDelay < System.currentTimeMillis()) {
                    callback.invoke(this)
                    currentTime
                } else {
                    it
                }
            } ?: kotlin.run {
                callback.invoke(this)
                currentTime
            }).also {
                setTag(id, it)
            }
        }
    }

}

