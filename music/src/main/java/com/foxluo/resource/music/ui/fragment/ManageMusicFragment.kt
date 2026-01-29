package com.foxluo.resource.music.ui.fragment

import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import com.foxluo.baselib.data.respository.BASE_URL
import com.foxluo.baselib.ui.MainPage
import com.foxluo.baselib.ui.fragment.BaseWebFragment

/**
 *    Author : 罗福林
 *    Date   : 2026/1/26
 *    Desc   :
 */
class ManageMusicFragment: BaseWebFragment(), MainPage {
    companion object {
        fun getFragment(showBackIv: Boolean = true, showProgress: Boolean = true) =
            ManageMusicFragment().apply {
                arguments = bundleOf("showBackIv" to showBackIv, "showProgress" to showProgress)
            }
    }

    private val apiUrl
        get() = BASE_URL + "/uploads/permanent/apk/mobile-music-manager.html"
    override fun showPlayView()=false
    override fun showNavBottom() = true
    override fun getLeftPlayPadding() = initPlayDragPadding()?.get(0) ?: 0
    override fun getTopPlayPadding() = initPlayDragPadding()?.get(1) ?: 0
    override fun getRightPlayPadding() = initPlayDragPadding()?.get(2) ?: 0
    override fun getBottomPlayPadding() = initPlayDragPadding()?.get(3) ?: 0
    private fun initPlayDragPadding():IntArray?{ return null}

    override fun initData() {
        super.initData()
        loadUrl(apiUrl)
    }

    override fun initView() {
        super.initView()
        binding.root.setBackgroundColor("#1890ff".toColorInt())
    }

    override fun initStatusBarView(): View? {
        return binding.root
    }
}