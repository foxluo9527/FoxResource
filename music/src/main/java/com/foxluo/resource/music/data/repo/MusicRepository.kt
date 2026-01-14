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
import com.foxluo.resource.music.data.database.ArtistDAO
import com.foxluo.resource.music.data.database.MusicDAO
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.result.ArtistResult
import com.foxluo.resource.music.data.result.MusicResult
import com.foxluo.resource.music.data.result.PlaylistResult
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
        val result = kotlin.runCatching { api?.favoriteMusic(id) }
        return result.toRequestResult()
    }

    suspend fun recordPlay(id: String,progress:Int): RequestResult {
        val result = kotlin.runCatching { api?.recordMusicPlay(id, mapOf("progress" to progress)) }
        return result.toRequestResult()
    }

    suspend fun getMusicList(page: Int, size: Int, keyword: String = "",sort: String = ""): RequestResult {
        val result = kotlin.runCatching { api?.getMusicList(page, size, keyword, sort) }
        return processNetworkResult(result)
    }

    suspend fun getHistoryMusicList(page: Int, size: Int): RequestResult {
        val result = kotlin.runCatching { api?.getHistoryMusicList(page, size) }
        return processNetworkResult(result, false)
    }

    // 本地查询方法
    suspend fun getLocalMusicList(page: Int, size: Int, keyword: String = ""): List<MusicEntity> {
        return musicDao.searchMusics(page, size, keyword).map { it.getMusicWithArtist() }
    }

    suspend fun getRecentMusicList(page: Int, size: Int): List<MusicEntity> {
        return musicDao.getMusics(Constant.TABLE_ALBUM_HISTORY_ID.toLong(), page, size)
            .map { it.getMusicWithArtist() }
    }

    // 新增方法
    fun getSearchMusicPager(keyword: String = "",sort: String = ""): Flow<PagingData<MusicEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                prefetchDistance = 2,
                initialLoadSize = 100
            ),
            pagingSourceFactory = { MusicPagingSource(this, keyword,sort) }
        ).flow
    }

    fun getRecentMusicPager(): Flow<PagingData<MusicEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                prefetchDistance = 2,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { RecentMusicPagingSource(this) }
        ).flow
    }

    private suspend fun processNetworkResult(
        result: Result<BaseListResponse<MusicResult>?>,
        cacheMusic: Boolean = true
    ): RequestResult {
        val resultData = result.getOrNull()
        val resultError = result.exceptionOrNull()
        val dataResult = ListData<MusicEntity>()
        result.getOrNull()?.data?.let { data ->
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
        return if (resultData?.success == true) {
            RequestResult.Success(dataResult, resultData.message)
        } else {
            RequestResult.Error(
                resultData?.code ?: 201,
                resultData?.message ?: resultError?.message ?: "网络连接错误,请稍后重试"
            )
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
            RequestResult.Error(result?.code,result?.message ?: "网络连接错误")
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
            RequestResult.Error(result?.code,result?.message ?: "网络连接错误")
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
        val result = kotlin.runCatching { api?.postMusicComment(map) }
        return result.toRequestResult()
    }

    suspend fun likeMusicComment(commentId: String): RequestResult {
        val result = kotlin.runCatching { api?.likeMusicComment(commentId) }
        return result.toRequestResult()
    }

    suspend fun getSearchHotKeywords(limit: Int = 10): RequestResult {
        val result = kotlin.runCatching { api?.getSearchHotKeywords("music", limit) }
        return result.toRequestResult()
    }

    suspend fun getPlaylistDetail(id: String): RequestResult {
        val result = kotlin.runCatching { api?.getPlaylistDetail(id) }
        return result.toRequestResult()
    }

    /**
     * 获取播放列表
     * @param isRecommend 是否推荐列表
     */
    suspend fun getPlaylistList(isRecommend: Boolean, page: Int, limit: Int): RequestResult {
        val result = kotlin.runCatching {
            if (isRecommend) api?.getRecommendedPlaylistList(
                page,
                limit
            ) else api?.getPlaylistList(page, limit)
        }
        return result.toRequestResult()
    }

    /**
     * 获取播放列表分页数据
     */
    fun getPlaylistPaging(isRecommend: Boolean): Flow<PagingData<PlaylistResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                prefetchDistance = 2,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { PlayListPagingSource(this, isRecommend) }
        ).flow
    }

    fun getArtistPaging(keyword: String, tagId: String? = null): Flow<PagingData<ArtistResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                prefetchDistance = 2,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { ArtistPagingSource(this, keyword, tagId) }
        ).flow
    }

    suspend fun getArtistList(
        page: Int,
        limit: Int,
        keyword: String,
        tagId: String? = null
    ): RequestResult {
        val result = kotlin.runCatching { api?.getArtistList(page, limit, keyword, tagId) }
        return result.toRequestResult()
    }
}