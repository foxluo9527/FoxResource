package com.foxluo.resource.music.ui.fragment

import androidx.viewbinding.ViewBinding
import com.foxluo.baselib.ui.MainPage

/**
 *    Author : 罗福林
 *    Date   : 2026/1/5
 *    Desc   :
 */
abstract class MainPageMusicFragment<Binding : ViewBinding> : BaseMusicFragment<Binding>(), MainPage {
    override fun getLeftPlayPadding() = initPlayDragPadding()?.get(0) ?: 0
    override fun getTopPlayPadding() = initPlayDragPadding()?.get(1) ?: 0
    override fun getRightPlayPadding() = initPlayDragPadding()?.get(2) ?: 0
    override fun getBottomPlayPadding() = initPlayDragPadding()?.get(3) ?: 0

    open fun initPlayDragPadding():IntArray?{ return null}
}