package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import kotlinx.coroutines.flow.collectLatest

class RecommendMusicViewModel : BaseViewModel() {
    private val db by lazy{
        MusicModuleInitializer.musicDb
    }

    private val repo by lazy {
        MusicRepository(db.musicDao(), db.artistDao())
    }

    val musicPager by lazy {
        MutableStateFlow<PagingData<MusicEntity>>(PagingData.empty())
    }

    fun loadMusic(keyword: String = "") {
        viewModelScope.launch {
            repo.getSearchMusicPager(keyword)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    musicPager.value = pagingData
                }
        }
    }
}