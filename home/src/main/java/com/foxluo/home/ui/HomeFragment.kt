package com.foxluo.home.ui

import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foxluo.baselib.R
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.home.databinding.FragmentHomeBinding
import com.foxluo.resource.music.ui.fragment.MainMusicFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.xuexiang.xui.widget.textview.marqueen.MarqueeFactory
import com.xuexiang.xui.widget.textview.marqueen.SimpleNoticeMF
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : MainPageFragment<FragmentHomeBinding>() {
    private val tabs by lazy {
        arrayOf(getString(R.string.music))
    }

    private val fragments by lazy {
        arrayOf(MainMusicFragment())
    }

    private val marqueeFactory by lazy {
        SimpleNoticeMF(getContext())
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
        binding.tvSearch.setMarqueeFactory(marqueeFactory)
    }

    override fun onResume() {
        super.onResume()
        binding.tvSearch.startFlipping()
    }

    override fun onPause() {
        super.onPause()
        binding.tvSearch.stopFlipping()
    }

    override fun initListener() {
        // 添加搜索点击事件
        binding.llSearch.setOnClickListener {
            EventViewModel.sendSearchPageEvent(true)
        }
    }

    override fun initObserver() {
        lifecycleScope.launch {
            EventViewModel.hotKeywords.collectLatest { list ->
                marqueeFactory.setData(list.map { it.keyword })
            }
        }
    }

    override fun initPlayDragPadding(): IntArray? {
        return intArrayOf(20, 50, 20, 0)
    }

    override fun initBinding() = FragmentHomeBinding.inflate(layoutInflater)

    override fun initStatusBarView(): View? {
        return binding.main
    }

    override fun showPlayView() = true
}