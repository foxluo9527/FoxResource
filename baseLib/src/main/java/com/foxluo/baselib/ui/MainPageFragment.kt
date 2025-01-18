package com.foxluo.baselib.ui

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding

abstract class MainPageFragment<Binding:ViewBinding>: BaseBindingFragment<Binding>() {
    var leftPlayPadding = 0
    var topPlayPadding = 0
    var rightPlayPadding = 0
    var bottomPlayPadding = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPlayDragPadding()?.runCatching {
            leftPlayPadding = get(0)
            topPlayPadding = get(1)
            rightPlayPadding = get(2)
            bottomPlayPadding = get(3)
        }
    }

    open fun initPlayDragPadding():IntArray?{ return null}

    abstract fun showPlayView(): Boolean
}