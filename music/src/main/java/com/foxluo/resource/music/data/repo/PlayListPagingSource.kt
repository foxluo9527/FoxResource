package com.foxluo.resource.music.data.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.result.PlaylistResult

/**
 *    Author : 罗福林
 *    Date   : 2026/1/14
 *    Desc   :
 */
class PlayListPagingSource(
    private val musicRepository: MusicRepository,
    private val isRecommend: Boolean
) : PagingSource<Int, PlaylistResult>() {
    override fun getRefreshKey(state: PagingState<Int, PlaylistResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlaylistResult> {
        return try {
            val page = params.key ?: 1
            val result = musicRepository.getPlaylistList(isRecommend, page, params.loadSize)
            when (result) {
                is RequestResult.Success -> {
                    // 网络成功，返回分页数据
                    result.data ?: emptyList()
                }

                is RequestResult.Error -> {
                    return LoadResult.Error(result.getError())
                }
            }.let { data ->
                val prevKey = if (page > 1) page - 1 else null
                val nextKey = if (data.isNotEmpty()) page + 1 else null
                LoadResult.Page(
                    data = data,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}