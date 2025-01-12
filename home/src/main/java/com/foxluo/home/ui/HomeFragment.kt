package com.foxluo.home.ui

import android.view.View
import androidx.core.os.bundleOf
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.home.databinding.FragmentHomeBinding
import com.foxluo.baselib.R
import com.foxluo.baselib.ui.fragment.TempFragment
import com.foxluo.resource.music.ui.fragment.MainMusicFragment
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : MainPageFragment<FragmentHomeBinding>() {
    private val tabs by lazy {
        arrayOf(getString(R.string.music), getString(R.string.video), getString(R.string.novel))
    }

    private val fragments by lazy {
        arrayOf(MainMusicFragment(), TempFragment().apply {
            arguments = bundleOf("type" to "功能开发中~")
        }, TempFragment().apply {
            arguments = bundleOf("type" to "功能开发中~")
        })
    }

    override fun initView() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragments[position]

            override fun getItemCount() = fragments.size
        }
        binding.homeViewpager.adapter = adapter
        TabLayoutMediator(binding.homeTab, binding.homeViewpager) { tab, position ->
            tab.text = tabs[position]
            tab.view.setOnLongClickListener{ true }
            tab.view.tooltipText = null
        }.apply {
            this.attach()
        }
    }

    override fun initPlayDragPadding(): IntArray? {
        return intArrayOf(20, 50, 20, 0)//todo 待视图完全确认后填写播放控件可拖动区域
    }

    override fun initBinding() = FragmentHomeBinding.inflate(layoutInflater)

    override fun initStatusBarView(): View? {
        return binding.main
    }

    override fun showPlaView() = true
}