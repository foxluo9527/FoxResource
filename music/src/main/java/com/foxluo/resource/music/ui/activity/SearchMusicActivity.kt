package com.foxluo.resource.music.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.util.ViewExt.fastClick
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.R
import com.foxluo.resource.music.data.domain.viewmodel.SearchMusicViewModel
import com.foxluo.resource.music.databinding.ActivitySearchMusicBinding
import com.foxluo.resource.music.ui.adapter.SearchHotKeywordAdapter
import com.foxluo.resource.music.ui.fragment.SearchResultFragment
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager

class SearchMusicActivity : BaseBindingActivity<ActivitySearchMusicBinding>() {

    private val vm by viewModels<SearchMusicViewModel>()

    private val hotKeywordAdapter by lazy {
        SearchHotKeywordAdapter()
    }

    override fun initBinding() = ActivitySearchMusicBinding.inflate(layoutInflater)

    override fun initStatusBarView(): View {
        return binding.main
    }

    override fun initView() {
        // 设置搜索热词RecyclerView
        binding.rvHotKeywords.layoutManager = LinearLayoutManager(this)
        binding.rvHotKeywords.adapter = hotKeywordAdapter
    }

    override fun initListener() {
        // 返回按钮点击事件
        binding.btnBack.fastClick {
            finish()
        }

        // 搜索按钮点击事件
        binding.btnSearch.fastClick {
            performSearch()
        }

        // 清空历史按钮点击事件
        binding.btnClearHistory.fastClick {
            clearSearchHistory()
        }

        // 搜索输入框文本变化事件
        binding.etSearch.addTextChangedListener {
            val keyword = it.toString().trim()
            if (keyword.isEmpty()) {
                showSearchContent()
            }
        }

        // 搜索输入框键盘搜索按钮点击事件
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
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
    }

    override fun initObserver() {
        // 观察搜索热词数据
        vm.hotKeywords.observe(this) {
            hotKeywordAdapter.setData(it)
        }
    }

    override fun onResume() {
        super.onResume()
        // 加载搜索热词
        vm.loadHotKeywords()
        // 显示搜索历史
        showSearchHistory()
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
    }

    /**
     * 显示搜索结果
     */
    private fun showSearchResult(keyword: String) {
        // 隐藏搜索内容区域
        binding.searchContent.visible(false)
        // 显示搜索结果容器
        binding.container.visible(true)

        // 传递搜索关键词到搜索结果Fragment
        val bundle = Bundle().apply {
            putString("keyword", keyword)
        }

        // 跳转到搜索结果Fragment
        val fragment = SearchResultFragment()
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    /**
     * 显示搜索内容（热词和历史）
     */
    private fun showSearchContent() {
        binding.searchContent.visible(true)
        binding.container.visible(false)
    }

    /**
     * 显示搜索历史
     */
    private fun showSearchHistory() {
        val history = getSearchHistory()
        if (history.isNotEmpty()) {
            binding.historyContainer.visible(true)
            binding.historyContainer.removeAllViews()

            // 动态添加搜索历史项
            history.forEach { keyword ->
                val historyItem = TextView(this).apply {
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
        getSharedPreferences("search_history", MODE_PRIVATE).edit {
            putStringSet("history", history.toSet())
        }
    }

    /**
     * 获取搜索历史
     */
    private fun getSearchHistory(): List<String> {
        return getSharedPreferences("search_history", MODE_PRIVATE)
            .getStringSet("history", emptySet())?.toList() ?: emptyList()
    }

    /**
     * 清空搜索历史
     */
    private fun clearSearchHistory() {
        getSharedPreferences("search_history", MODE_PRIVATE).edit {
            remove("history")
        }
        showSearchHistory()
    }
}
