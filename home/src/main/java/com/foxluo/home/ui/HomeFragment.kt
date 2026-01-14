package com.foxluo.home.ui

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.home.databinding.FragmentHomeBinding
import com.foxluo.resource.music.ui.fragment.MainMusicFragment
import com.foxluo.resource.music.ui.fragment.SearchMusicFragment
import com.xuexiang.xui.widget.textview.marqueen.SimpleNoticeMF
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : MainPageFragment<FragmentHomeBinding>() {
    private val mainMusicFragment by lazy {
        MainMusicFragment()
    }

    private val searchFragment by lazy {
        SearchMusicFragment()
    }

    private val marqueeFactory by lazy {
        SimpleNoticeMF(context)
    }

    override fun initView() {
        childFragmentManager.beginTransaction()
            .replace(com.foxluo.home.R.id.home_container, mainMusicFragment)
            .commit()
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
            EventViewModel.showMainPageFragment.value = searchFragment to System.currentTimeMillis()
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

    override fun showNavBottom() = true
}