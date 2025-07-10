package com.foxluo.resource.music.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.BaseListResponse
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.baselib.util.Constant
import com.foxluo.resource.music.data.api.MusicApi
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.dao.ArtistDAO
import com.foxluo.resource.music.data.dao.MusicDAO
import com.foxluo.resource.music.data.result.MusicResult
import com.foxluo.resource.music.data.result.toCommentList
import com.foxluo.resource.music.data.result.toCommentReplay
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val musicDao: MusicDAO,
    private val artistDao: ArtistDAO
) : BaseRepository() {
    private val api by lazy {
        createApi<MusicApi>()
    }

    suspend fun favoriteMusic(id: String): RequestResult {
        val result = kotlin.runCatching { api?.favoriteMusic(id) }.getOrNull()
        return result.toRequestResult()
    }

    suspend fun recordPlay(id: String): RequestResult {
        val result = kotlin.runCatching { api?.recordMusicPlay(id) }.getOrNull()
        return result.toRequestResult()
    }

    suspend fun getMusicList(page: Int, size: Int, keyword: String = ""): RequestResult {
        val result = kotlin.runCatching { api?.getMusicList(page, size, keyword) }.getOrNull()
        return processNetworkResult(result)
    }

    suspend fun getHistoryMusicList(page: Int, size: Int): RequestResult {
        val result = kotlin.runCatching { api?.getHistoryMusicList(page, size) }.getOrNull()
        return processNetworkResult(result, false)
    }

    // 本地查询方法
    suspend fun getLocalMusicList(page: Int, size: Int, keyword: String = ""): List<MusicData> {
        return musicDao.searchMusics(page, size, keyword).map { it.getMusicWithArtist() }
    }

    suspend fun getRecentMusicList(page: Int, size: Int): List<MusicData> {
        return musicDao.getMusics(Constant.TABLE_ALBUM_HISTORY_ID.toLong(), page, size)
            .map { it.getMusicWithArtist() }
    }

    // 新增方法
    fun getSearchMusicPager(keyword: String = ""): Flow<PagingData<MusicData>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 2,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { MusicPagingSource(this, keyword) }
        ).flow
    }

    fun getRecentMusicPager(): Flow<PagingData<MusicData>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 2,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { RecentMusicPagingSource(this) }
        ).flow
    }

    private suspend fun processNetworkResult(
        result: BaseListResponse<MusicResult>?,
        cacheMusic: Boolean = true
    ): RequestResult {
        val dataResult = ListData<MusicData>()
        result?.data?.let { data ->
            data.list?.map { it.toMusicData() }?.also { musics ->
                // 插入数据库
                if (cacheMusic) musicDao.insertMusics(musics)
                dataResult.apply {
                    list = musics
                    total = data.total
                    current = data.current
                    pageSize = data.pageSize
                }
            }
        }
        return if (result?.success == true) {
            RequestResult.Success(dataResult, result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络错误")
        }
    }

    suspend fun getMusicComment(musicId: String, page: Int, size: Int): RequestResult {
        val result = kotlin.runCatching { api?.getMusicComments(musicId, page, size) }.getOrNull()
        val dataResult = ListData<CommentAdapter.CommentBean>()
        val dataList = result?.data?.let { data ->
            data.list?.map {
                it.toCommentList()
            }?.flatten().also {
                dataResult.apply {
                    list = it
                    total = data.total
                    current = data.current
                    pageSize = data.pageSize
                }
            }
        }
        return if (dataList != null) {
            RequestResult.Success<ListData<CommentAdapter.CommentBean>?>(dataResult, result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun getMusicCommentReply(commentId: String, page: Int, size: Int): RequestResult {
        val result = kotlin.runCatching { api?.getMusicReplies(commentId, page, size) }.getOrNull()
        val dataResult = ListData<CommentAdapter.CommentBean>()
        val dataList = result?.data?.let { data ->
            data.list?.map {
                it.toCommentReplay(
                    commentId,
                    data.current * data.pageSize < data.total && data.list?.lastOrNull() == it
                )
            }.also {
                dataResult.apply {
                    list = it
                    total = data.total
                    current = data.current
                    pageSize = data.pageSize
                }
            }
        }
        return if (dataList != null) {
            RequestResult.Success<ListData<CommentAdapter.CommentBean>?>(dataResult, result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun postMusicComment(
        musicId: String,
        content: String,
        commentId: String?
    ): RequestResult {
        val map = mutableMapOf("music_id" to musicId, "content" to content)
        if (commentId != null) {
            map["parent_id"] = commentId
        }
        val result = kotlin.runCatching { api?.postMusicComment(map) }.getOrNull()
        return result.toRequestResult()
    }

    suspend fun likeMusicComment(commentId: String): RequestResult {
        val result = kotlin.runCatching { api?.likeMusicComment(commentId) }.getOrNull()
        return result.toRequestResult()
    }
}