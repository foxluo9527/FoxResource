package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    val playingAlbum by lazy {
        MutableLiveData<Pair<AlbumData,Int>>()
    }

    fun recordPlayMusicChanged(musicId: String) {
        viewModelScope.launch {
            repo.recordPlay(musicId)
        }
    }
}