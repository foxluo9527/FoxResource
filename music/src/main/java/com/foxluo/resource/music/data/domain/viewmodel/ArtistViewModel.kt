package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import com.foxluo.resource.music.data.repo.MusicRepository
import com.foxluo.resource.music.data.result.ArtistResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 *    Author : 罗福林
 *    Date   : 2026/1/14
 *    Desc   :
 */
class ArtistViewModel() : BaseViewModel() {
    private val db by lazy {
        MusicModuleInitializer.musicDb
    }
    private val repository by lazy {
        MusicRepository(db.musicDao(), db.artistDao())
    }

    val artistPager by lazy {
        MutableStateFlow<PagingData<ArtistResult>>(PagingData.empty())
    }

    fun loadArtistList(
        keyword: String = "",
        tagId: String? = null
    ) {
        viewModelScope.launch {
            repository.getArtistPaging(keyword, tagId)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    artistPager.value = pagingData
                }
        }
    }

    fun getArtistList(
        keyword: String = "",
        tagId: String? = null,
        success: (List<ArtistResult>) -> Unit,
        error:(Throwable)-> Unit
    ) {
        viewModelScope.launch {
            val result = repository.getArtistList(1, 10, keyword, tagId)
            if (result is RequestResult.Success) {
                success(result.data?.list ?: listOf())
            } else if (result is RequestResult.Error) {
                error(result.getError())
            }
        }
    }
}