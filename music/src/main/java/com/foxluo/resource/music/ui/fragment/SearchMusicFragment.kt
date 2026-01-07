package com.foxluo.resource.music.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.baselib.util.ViewExt.fastClick
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.R
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.data.domain.viewmodel.SearchMusicViewModel
import com.foxluo.resource.music.databinding.FragmentSearchMusicBinding
import com.foxluo.resource.music.ui.adapter.SearchHotKeywordAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchMusicFragment : MainPageFragment<FragmentSearchMusicBinding>() {

    private lateinit var vm: SearchMusicViewModel

    private val hotKeywordAdapter by lazy {
        SearchHotKeywordAdapter()
    }

    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
    }

    // 当前是否显示搜索结果
    private var isShowingResult = false

    override fun initBinding() = FragmentSearchMusicBinding.inflate(layoutInflater)

    override fun initView() {
        // 设置搜索热词RecyclerView
        binding.rvHotKeywords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHotKeywords.adapter = hotKeywordAdapter
        showSearchHistory()
    }

    override fun initStatusBarView(): View? {
        return binding.main
    }

    override fun initListener() {
        // 返回按钮点击事件
        binding.btnBack.fastClick {
            if (isShowingResult) {
                showSearchContent()
                binding.etSearch.text?.clear()
            } else {
                EventViewModel.showMainPageFragment.value = null
            }
        }

        // 搜索按钮点击事件
        binding.btnSearch.fastClick {
            performSearch()
        }

        // 清空历史按钮点击事件
        binding.btnClearHistory.fastClick {
            clearSearchHistory()
        }

        // 搜索输入框键盘搜索按钮点击事件
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                performSearch()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        // 搜索热词点击事件
        hotKeywordAdapter.setOnItemClickListener {
            binding.etSearch.setText(it.keyword)
            performSearch()
        }

        // 设置返回键拦截
        setupBackPressedCallback()
    }

    override fun initObserver() {
        // 观察逻辑已移至onViewCreated
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化ViewModel
        vm = ViewModelProvider(requireActivity()).get(SearchMusicViewModel::class.java)
        // 观察搜索热词数据
        lifecycleScope.launch {
            EventViewModel.hotKeywords.collectLatest { hotKeywords ->
                hotKeywordAdapter.setData(hotKeywords)
            }
        }
    }

    /**
     * 设置返回键拦截
     */
    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isShowingResult) {
                    showSearchContent()
                    binding.etSearch.text?.clear()
                } else {
                    EventViewModel.showMainPageFragment.value = null
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    /**
     * 执行搜索
     */
    private fun performSearch() {
        val keyword = binding.etSearch.text.toString().trim()
        if (keyword.isEmpty()) return

        // 保存搜索历史
        saveSearchHistory(keyword)
        // 显示搜索结果
        showSearchResult(keyword)
        KeyboardUtils.hideSoftInput(requireActivity())
    }

    /**
     * 显示搜索结果
     */
    private fun showSearchResult(keyword: String) {
        // 隐藏搜索内容区域
        binding.searchContent.visible(false)
        // 显示搜索结果容器
        binding.container.visible(true)
        isShowingResult = true

        // 传递搜索关键词到搜索结果Fragment
        val bundle = Bundle().apply {
            putString("keyword", keyword)
        }

        // 跳转到搜索结果Fragment
        val fragment = SearchResultFragment()
        fragment.arguments = bundle
        childFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
        EventViewModel.showMainPageFragment.value = this
    }

    /**
     * 显示搜索内容（热词和历史）
     */
    private fun showSearchContent() {
        musicViewModel.loadHotKeywords()
        binding.searchContent.visible(true)
        binding.container.visible(false)
        isShowingResult = false
        showSearchHistory()
        EventViewModel.showMainPageFragment.value = this
        KeyboardUtils.hideSoftInput(requireActivity())
    }

    /**
     * 显示搜索历史
     */
    private fun showSearchHistory() {
        val history = getSearchHistory()
        if (history.isNotEmpty()) {
            binding.searchHistory.visible(true)
            binding.historyContainer.visible(true)
            binding.historyContainer.removeAllViews()

            // 动态添加搜索历史项
            history.forEach { keyword ->
                val historyItem = TextView(requireContext()).apply {
                    text = keyword
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.black, null))
                    setBackgroundResource(R.drawable.shape_gray_trans_30_r8)
                    setPadding(
                        resources.getDimensionPixelSize(R.dimen.dp_10),
                        resources.getDimensionPixelSize(R.dimen.dp_10),
                        resources.getDimensionPixelSize(R.dimen.dp_10),
                        resources.getDimensionPixelSize(R.dimen.dp_10)
                    )
                    layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = resources.getDimensionPixelSize(R.dimen.dp_4)
                        marginEnd = resources.getDimensionPixelSize(R.dimen.dp_4)
                        topMargin = resources.getDimensionPixelSize(R.dimen.dp_4)
                        bottomMargin = resources.getDimensionPixelSize(R.dimen.dp_4)
                    }

                    // 设置点击事件
                    setOnClickListener {
                        binding.etSearch.setText(keyword)
                        performSearch()
                    }
                }

                binding.historyContainer.addView(historyItem)
            }
        } else {
            binding.searchHistory.visible(false)
            binding.historyContainer.visible(false)
        }
    }

    /**
     * 保存搜索历史
     */
    private fun saveSearchHistory(keyword: String) {
        // 简单实现，实际项目中可以使用SharedPreferences或数据库
        val history = getSearchHistory().toMutableList()
        // 如果已经存在，先移除
        history.remove(keyword)
        // 添加到开头
        history.add(0, keyword)
        // 最多保存10条
        if (history.size > 10) {
            history.removeAt(history.size - 1)
        }
        // 保存到SharedPreferences
        requireContext().getSharedPreferences("search_history", 0).edit {
            putStringSet("history", history.toSet())
        }
    }

    /**
     * 获取搜索历史
     */
    private fun getSearchHistory(): List<String> {
        return requireContext().getSharedPreferences("search_history", 0)
            .getStringSet("history", emptySet())?.toList() ?: emptyList()
    }

    /**
     * 清空搜索历史
     */
    private fun clearSearchHistory() {
        requireContext().getSharedPreferences("search_history", 0).edit {
            remove("history")
        }
        showSearchHistory()
    }

    /**
     * 根据当前显示状态决定是否显示playview
     */
    override fun showPlayView(): Boolean {
        return isShowingResult
    }

    override fun initPlayDragPadding(): IntArray? {
        return intArrayOf(20, 50, 20, 0)
    }
}
