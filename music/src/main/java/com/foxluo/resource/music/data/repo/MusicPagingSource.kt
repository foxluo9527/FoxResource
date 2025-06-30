package com.foxluo.resource.music.data.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.resource.music.data.bean.MusicData
import java.util.concurrent.ConcurrentHashMap

class MusicPagingSource(
    private val repo: MusicRepository,
    private val keyword: String = ""
) : PagingSource<Int, MusicData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MusicData> {
        return try {
            // 尝试网络请求
            val page = params.key ?: 1
            val result = repo.getMusicList(page, params.loadSize, keyword)

            when (result) {
                is RequestResult.Success<*> -> {
                    // 网络成功，返回分页数据
                    val data = (result as? RequestResult.Success<ListData<MusicData>>)?.data?.list
                        ?: emptyList()
                    LoadResult.Page(
                        data = data,
                        prevKey = if (page > 1) page - 1 else null,
                        nextKey = if (data.isNotEmpty()) page + 1 else null
                    )
                }

                is RequestResult.Error -> {
                    // 网络失败，回退本地数据
                    val localData = repo.getLocalMusicList(page, params.loadSize, keyword)
                    LoadResult.Page(
                        data = localData,
                        prevKey = if (page > 1) page - 1 else null,
                        nextKey = if (localData.isNotEmpty()) page + 1 else null
                    )
                }
            }
        } catch (e: Exception) {
            // 异常情况回退本地
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MusicData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}