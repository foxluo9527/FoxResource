package com.foxluo.baselib.util

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

object ViewExt {
    //扩展函数，view隐藏
    fun View.gone() {
        visibility = View.GONE
    }

    //扩展函数，view显示
    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.inVisible(){
        visibility = View.INVISIBLE
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

    /**
     * 歌词视图触摸监听器
     * 纵向滑动时拦截事件（解决与ViewPager等父容器的滑动冲突）
     */
    fun getLyricViewTouchEventListener() = object : View.OnTouchListener {
        private val touchSlop = ViewConfiguration.getTouchSlop() // 系统默认触摸阈值

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    disallowParentInterceptTouchEvent(v?.parent as? View, true)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    disallowParentInterceptTouchEvent(v?.parent as? View, false)
                }
            }
            return v?.onTouchEvent(event) ?: false
        }
    }

    /**
     * 递归屏蔽所有父布局拦截
     */
    fun disallowParentInterceptTouchEvent(v: View?, disallow: Boolean) {
        v ?: return
        if (v is ViewPager2) {
            v.requestDisallowInterceptTouchEvent(disallow)
        }
        disallowParentInterceptTouchEvent(v.parent as? View, disallow)
    }
}

