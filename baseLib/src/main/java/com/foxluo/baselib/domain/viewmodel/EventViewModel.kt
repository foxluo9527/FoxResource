package com.foxluo.baselib.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import com.foxluo.baselib.domain.bean.SearchHotKeyword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 *    Author : 罗福林
 *    Date   : 2026/1/4
 *    Desc   : 全局事件管理
 */
object EventViewModel {

    // 搜索事件：true 表示显示搜索页，false 表示隐藏搜索页
    private val _searchPageEvent = MutableStateFlow(false to 0L)
    val searchPageEvent: StateFlow<Pair<Boolean, Long>> = _searchPageEvent

    val hotKeywords by lazy {
        MutableStateFlow<List<SearchHotKeyword>>(listOf())
    }

    /**
     * 发送搜索事件
     */
    fun sendSearchPageEvent(show: Boolean) {
        _searchPageEvent.value = show to System.currentTimeMillis()
    }

    /**
     * 清除搜索事件
     */
    fun clearSearchEvent() {
        _searchPageEvent.value = false to 0L
    }

    val appInForeground by lazy {
        MutableLiveData<Boolean>(true)
    }
}