package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.room.Room
import com.blankj.utilcode.util.Utils
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.foxluo.resource.music.data.db.AppDatabase
import kotlinx.coroutines.flow.collectLatest

class RecentMusicViewModel : BaseViewModel() {
    val db = Room.databaseBuilder(
        Utils.getApp(),
        AppDatabase::class.java, "fox_resource_db"
    ).build()

    private val repo by lazy {
        MusicRepository(db.musicDao(), db.artistDao())
    }

    val musicPager by lazy {
        MutableStateFlow<PagingData<MusicData>>(PagingData.empty())
    }

    fun loadMusic() {
        viewModelScope.launch {
            repo.getRecentMusicPager()
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    musicPager.value = pagingData
                }
        }
    }
}