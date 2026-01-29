package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import com.foxluo.resource.music.data.repo.MusicRepository
import com.foxluo.resource.music.data.result.DashboardStats
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

    fun getPlaylist(
        isRecommend: Boolean,
        success: (List<PlaylistResult>) -> Unit,
        error: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.getPlaylistList(isRecommend, 1, 10)
            if (result is RequestResult.Success) {
                isLoading.value = false
                success(result.data ?: emptyList())
            } else if (result is RequestResult.Error) {
                isLoading.value = false
                error(result.getError())
            }
        }
    }

    fun createPlaylist(title: String, block: () -> Unit) {
        viewModelScope.launch {
            val result = repository.createPlaylist(title)
            if (result is RequestResult.Success) {
                block()
            } else if (result is RequestResult.Error) {
                toast.value = false to result.message
            }
        }
    }

    fun importPlaylist(url: String, block: () -> Unit) {
        viewModelScope.launch {
            val result = repository.importPlaylist(url)
            if (result is RequestResult.Success) {
                block()
            } else if (result is RequestResult.Error) {
                toast.value = false to result.message
            }
        }
    }

    fun getMusicStats(block: (DashboardStats?) -> Unit){
        viewModelScope.launch {
            repository.getDashboardStats().result(onSuccess = {
                block(it)
            }, onError = {
                toast.value = false to it
            })
        }
    }

    fun addMusicToPlaylist(id: String, ids: List<String>, block: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            repository.addMusicToPlaylist(id, ids).let {
                isLoading.value = false
                if (it is RequestResult.Success) {
                    block()
                    toast.value = true to it.message
                } else if (it is RequestResult.Error) {
                    toast.value = false to it.message
                }
            }
        }
    }
}