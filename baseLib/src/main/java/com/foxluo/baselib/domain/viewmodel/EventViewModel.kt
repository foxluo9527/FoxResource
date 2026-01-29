package com.foxluo.baselib.domain.viewmodel

import androidx.fragment.app.Fragment
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

    val showMainPageFragment = MutableStateFlow<Pair<Fragment?, Long>>(null to 0L)

    val mainPageStateChanged = MutableStateFlow<Long>(0L)

    val hotKeywords by lazy {
        MutableStateFlow<List<SearchHotKeyword>>(listOf())
    }

    val appInForeground by lazy {
        MutableLiveData<Boolean>(true)
    }

    val updatePlaylist by lazy {
        MutableLiveData<Long>(0L)
    }

    val deletePlaylist by lazy {
        MutableLiveData<String>(null)
    }
}