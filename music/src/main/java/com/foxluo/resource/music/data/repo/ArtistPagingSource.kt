package com.foxluo.resource.music.data.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.resource.music.data.result.ArtistResult

/**
 *    Author : 罗福林
 *    Date   : 2026/1/14
 *    Desc   :
 */
class ArtistPagingSource(
    private val repository: MusicRepository,
    private val keyword: String,
    private val tagId: String? = null
) : PagingSource<Int, ArtistResult>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArtistResult> {
        return try {
            val page = params.key ?: 1
            val result = repository.getArtistList(page, params.loadSize, keyword, tagId)
            when (result) {
                is RequestResult.Success<*> -> {
                    // 网络成功，返回分页数据
                    (result.data as? ListData<ArtistResult>)?.list ?: emptyList()
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

    override fun getRefreshKey(state: PagingState<Int, ArtistResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey ?: state.closestPageToPosition(
                anchorPosition
            )?.nextKey
        }
    }
}