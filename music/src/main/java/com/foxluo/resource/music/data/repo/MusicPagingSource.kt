package com.foxluo.resource.music.data.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.resource.music.data.database.MusicEntity

class MusicPagingSource(
    private val repo: MusicRepository,
    private val keyword: String = "",
    private val sort:String = ""
) : PagingSource<Int, MusicEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MusicEntity> {
        return try {
            // 尝试网络请求
            val page = params.key ?: 1
            val result = repo.getMusicList(page, params.loadSize, keyword,sort)
            when (result) {
                is RequestResult.Success -> {
                    // 网络成功，返回分页数据
                    result.data?.list ?: emptyList()
                }

                is RequestResult.Error -> {
                    // 网络失败，返回本地数据,若无本地数据则返回错误
                    val localData = repo.getLocalMusicList(page, params.loadSize, keyword)
                    if (localData.isNullOrEmpty()) {
                        return LoadResult.Error(result.getError())
                    } else {
                        localData
                    }
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
            // 异常情况回退本地
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MusicEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}