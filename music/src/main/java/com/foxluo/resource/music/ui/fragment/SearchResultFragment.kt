package com.foxluo.resource.music.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.resource.music.R
import com.foxluo.resource.music.data.domain.viewmodel.SearchMusicViewModel
import com.foxluo.resource.music.databinding.FragmentSearchResultBinding
import com.foxluo.resource.music.ui.adapter.MusicListAdapter
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class SearchResultFragment : BaseBindingFragment<FragmentSearchResultBinding>() {

    private val keyword by lazy {
        arguments?.getString("keyword") ?: ""
    }

    private val vm by viewModels<SearchMusicViewModel>({
        requireActivity()
    })

    private val adapter by lazy {
        MusicListAdapter(true) { _, _ ->
            // 点击事件处理
        }
    }

    override fun initBinding(): FragmentSearchResultBinding {
        return FragmentSearchResultBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.rvSearchResult.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResult.adapter = adapter
    }

    override fun initListener() {
        // 可以在这里添加下拉刷新等监听
    }

    override fun initObserver() {
        // 观察搜索结果数据
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.musicPager.collect {pagingData ->
                adapter.submitData(lifecycle, pagingData)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 设置搜索关键词并加载搜索结果
        vm.keyword.value = keyword
        vm.loadMusic()
    }
}
