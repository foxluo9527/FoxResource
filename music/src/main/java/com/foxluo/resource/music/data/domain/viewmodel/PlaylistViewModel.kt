package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import com.foxluo.resource.music.data.repo.MusicRepository
import com.foxluo.resource.music.data.result.MusicResult
import com.foxluo.resource.music.data.result.PlaylistDetailResult
import com.foxluo.resource.music.data.result.PlaylistResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 *    Author : 罗福林
 *    Date   : 2026/1/14
 *    Desc   :
 */
class PlaylistViewModel() : BaseViewModel() {
    private val db by lazy {
        MusicModuleInitializer.musicDb
    }
    private val repository by lazy {
        MusicRepository(db.musicDao(), db.artistDao())
    }

    private val _playlistDetail = MutableLiveData<List<MusicResult>>()
    val playlistDetail: LiveData<List<MusicResult>> = _playlistDetail

    val playlistPager by lazy {
        MutableStateFlow<PagingData<PlaylistResult>>(PagingData.empty())
    }

    fun loadPlayList(isRecommend: Boolean) {
        viewModelScope.launch {
            repository.getPlaylistPaging(isRecommend)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    playlistPager.value = pagingData
                }
        }
    }

    fun getPlaylistDetail(playlistId: Long) {
        viewModelScope.launch {
            val result = repository.getPlaylistDetail(playlistId.toString())
            if (result is RequestResult.Success<*>) {
                _playlistDetail.value =
                    (result.data as? PlaylistDetailResult)?.tracks ?: emptyList()
            }
        }
    }

    fun getPlaylist(
        isRecommend: Boolean,
        success: (List<PlaylistResult>) -> Unit,
        error: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.getPlaylistList(isRecommend, 1, 10)
            if (result is RequestResult.Success<*>) {
                success((result.data as? List<PlaylistResult>) ?: emptyList())
            } else if (result is RequestResult.Error) {
                error(result.getError())
            }
        }
    }
}