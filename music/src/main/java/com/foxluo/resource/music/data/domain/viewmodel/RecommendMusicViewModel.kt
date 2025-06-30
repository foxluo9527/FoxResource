package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.room.Room
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import com.foxluo.resource.music.data.db.AppDatabase

class RecommendMusicViewModel : BaseViewModel() {
    val db = Room.databaseBuilder(
        Utils.getApp(),
        AppDatabase::class.java, "fox_resource_db"
    ).build()

    private val repo by lazy {
        MusicRepository(db.musicDao(), db.artistDao())
    }

    val musicPager = MutableStateFlow<PagingData<MusicData>>(PagingData.empty())

    fun loadMusic(keyword: String = "") {
        viewModelScope.launch {
            repo.getMusicPager(keyword)
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    musicPager.value = pagingData
                }
        }
    }
}