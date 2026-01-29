package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseUploadViewModel
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import com.foxluo.resource.music.data.repo.MusicRepository
import com.foxluo.resource.music.data.result.PlaylistDetailResult
import com.foxluo.resource.music.data.result.PlaylistResult
import com.foxluo.resource.music.data.result.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 *    Author : 罗福林
 *    Date   : 2026/1/14
 *    Desc   :
 */
class PlaylistDetailViewModel : BaseUploadViewModel() {
    private val db by lazy {
        MusicModuleInitializer.musicDb
    }
    private val repository by lazy {
        MusicRepository(db.musicDao(), db.artistDao())
    }

    private val _playlistDetail = MutableLiveData<PlaylistDetailResult>()
    val playlistDetail: LiveData<PlaylistDetailResult> = _playlistDetail

    fun setPlaylistDetail(playlist: PlaylistDetailResult){
        _playlistDetail.value = playlist
    }

    val playlistMusicPager by lazy {
        MutableStateFlow<PagingData<MusicEntity>>(PagingData.empty())
    }

    fun loadPlayListMusic(id: String) {
        viewModelScope.launch {
            repository.getPlaylistDetailPaging(id, _playlistDetail)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    playlistMusicPager.value = pagingData
                }
        }
    }

    fun deletePlaylist(id: String, block: () -> Unit) {
        viewModelScope.launch {
            repository.deletePlaylist(id).let {
                if (it is RequestResult.Success) {
                    block()
                    toast.value = true to it.message
                } else if (it is RequestResult.Error) {
                    toast.value = false to it.message
                }
            }
        }
    }
    
    fun updatePlaylist(playlist: PlaylistDetailResult, block: () -> Unit) {
        viewModelScope.launch {
            repository.updatePlaylist(playlist).let {
                if (it is RequestResult.Success) {
                    block()
                } else if (it is RequestResult.Error) {
                    toast.value = false to it.message
                }
            }
        }
    }
    
    fun getMusicTags(block: (List<Tag>) -> Unit) {
        viewModelScope.launch {
            repository.getMusicTags().let {
                if (it is RequestResult.Success) {
                    val tags = it.data?:listOf()
                    block(tags)
                } else if (it is RequestResult.Error) {
                    toast.value = false to it.message
                    block(emptyList())
                }
            }
        }
    }
}