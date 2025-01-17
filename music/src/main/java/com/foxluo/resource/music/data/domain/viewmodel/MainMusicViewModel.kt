package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.baselib.domain.viewmodel.UnPeekLiveData
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.bean.MusicData

class MainMusicViewModel: BaseViewModel() {
    var isCurrentMusicByUser = false

    val currentMusic by lazy {
        MutableLiveData<MusicData?>()
    }

    val playingAlbum by lazy {
        MutableLiveData<Pair<AlbumData,Int>>()
    }
}