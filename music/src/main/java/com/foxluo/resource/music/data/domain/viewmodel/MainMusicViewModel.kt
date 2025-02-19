package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.launch

class MainMusicViewModel: BaseViewModel() {
    private val repo by lazy {
        MusicRepository()
    }

    var isCurrentMusicByUser = false

    val currentMusic by lazy {
        MutableLiveData<MusicData?>()
    }

    val musicFavoriteState by lazy {
        MediatorLiveData<Boolean>().apply {
            addSource(currentMusic) {
                value = it?.isCollection
            }
        }
    }

    val playingAlbum by lazy {
        MutableLiveData<Pair<AlbumData,Int>>()
    }

    fun recordPlayMusicChanged(musicId: String) {
        viewModelScope.launch {
            repo.recordPlay(musicId)
        }
    }

    fun favoriteMusic(musicId: String) {
        if (isLoading.value == true) return
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.favoriteMusic(musicId)
            if (result is RequestResult.Success<*>) {
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
}