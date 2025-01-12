package com.foxluo.mine.ui

import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.mine.databinding.FragmentMineBinding

class MineFragment : MainPageFragment<FragmentMineBinding>() {
    override fun initBinding() = FragmentMineBinding.inflate(layoutInflater)
    override fun initPlayDragPadding(): IntArray? {
        return intArrayOf(20,0,20,0)//todo 待视图完全确认后填写播放控件可拖动区域
    }
    override fun showPlaView() = true
}