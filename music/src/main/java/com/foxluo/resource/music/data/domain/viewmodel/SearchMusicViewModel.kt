package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.foxluo.baselib.data.result.BaseListResponse
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import com.foxluo.resource.music.data.repo.MusicRepository
import com.foxluo.resource.music.data.result.SearchHotKeyword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchMusicViewModel : BaseViewModel() {
    private val db by lazy{
        MusicModuleInitializer.musicDb
    }

    private val repo by lazy {
        MusicRepository(db.musicDao(), db.artistDao())
    }

    val musicPager by lazy {
        MutableStateFlow<PagingData<MusicEntity>>(PagingData.empty())
    }

    val keyword by lazy {
        MutableLiveData("")
    }

    val hotKeywords by lazy {
        MutableLiveData<List<SearchHotKeyword>>()
    }

    fun loadMusic() {
        viewModelScope.launch {
            repo.getSearchMusicPager(keyword.value ?: "", "recommend")
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    musicPager.value = pagingData
                }
        }
    }

    fun loadHotKeywords(limit: Int = 10) {
        viewModelScope.launch {
            val result = repo.getSearchHotKeywords(limit)
            if (result is RequestResult.Success<*>) {
                hotKeywords.value = result.data as? List<SearchHotKeyword> ?: emptyList()
            }
        }
    }
}