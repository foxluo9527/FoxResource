package com.foxluo.resource.community.ui

import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.resource.community.databinding.FragmentCommunityBinding

class CommunityFragment : MainPageFragment<FragmentCommunityBinding>() {
    override fun initBinding() = FragmentCommunityBinding.inflate(layoutInflater)
    override fun showPlaView() = true
    override fun initPlayDragPadding(): IntArray? {
        return intArrayOf(20,0,20,0)//todo 待视图完全确认后填写播放控件可拖动区域
    }
}