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

    val showMainPageFragment = MutableStateFlow<Fragment?>(null)

    val hotKeywords by lazy {
        MutableStateFlow<List<SearchHotKeyword>>(listOf())
    }

    val appInForeground by lazy {
        MutableLiveData<Boolean>(true)
    }
}