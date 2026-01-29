package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.bean.SearchHotKeyword
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.resource.music.data.database.AlbumEntity
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainMusicViewModel: BaseViewModel() {
    private val db by lazy{
        MusicModuleInitializer.musicDb
    }

    private val repo by lazy {
        MusicRepository(db.musicDao(),db.artistDao())
    }

    var isCurrentMusicByUser = false

    val currentMusic by lazy {
        MutableLiveData<MusicEntity?>()
    }

    val musicFavoriteState by lazy {
        MediatorLiveData<Boolean>().apply {
            addSource(currentMusic) {
                value = it?.isCollection?:false
            }
        }
    }

    val playingAlbum by lazy {
        MutableLiveData<AlbumEntity>()
    }

    fun recordPlayMusicChanged(musicId: String, progress: Int) {
        viewModelScope.launch {
            repo.recordPlay(musicId, progress)
        }
    }

    fun favoriteMusic(musicId: String) {
        if (isLoading.value == true) return
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.favoriteMusic(musicId)
            if (result is RequestResult.Success) {
                isCurrentMusicByUser = false
                currentMusic.postValue(currentMusic.value?.apply {
                    isCollection = !isCollection
                })
            } else if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            }
            isLoading.postValue(false)
        }
    }

    fun loadHotKeywords(limit: Int = 10) {
        viewModelScope.launch {
            val result = repo.getSearchHotKeywords(limit)
            if (result is RequestResult.Success) {
                EventViewModel.hotKeywords.update {
                    result.data ?: emptyList()
                }
            }
        }
    }
}