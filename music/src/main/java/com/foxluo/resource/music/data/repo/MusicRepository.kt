package com.foxluo.resource.music.data.repo

import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.blankj.utilcode.util.JsonUtils
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.BaseListResponse
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.bean.SearchHotKeyword
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.baselib.util.Constant
import com.foxluo.baselib.util.GsonUtil
import com.foxluo.resource.music.data.api.MusicApi
import com.foxluo.resource.music.data.database.ArtistDAO
import com.foxluo.resource.music.data.database.MusicDAO
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.result.ArtistResult
import com.foxluo.resource.music.data.result.DashboardStats
import com.foxluo.resource.music.data.result.MusicComment
import com.foxluo.resource.music.data.result.MusicResult
import com.foxluo.resource.music.data.result.PlaylistDetailResult
import com.foxluo.resource.music.data.result.PlaylistResult
import com.foxluo.resource.music.data.result.Tag
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

    suspend fun favoriteMusic(id: String): RequestResult<Unit?> {
        val result = kotlin.runCatching { api?.favoriteMusic(id) }
        return result.toRequestResult()
    }

    suspend fun recordPlay(id: String,progress:Int): RequestResult<Unit?> {
        val result = kotlin.runCatching { api?.recordMusicPlay(id, mapOf("progress" to progress)) }
        return result.toRequestResult()
    }

    suspend fun getMusicList(page: Int, size: Int, keyword: String = "",sort: String = ""): RequestResult<ListData<MusicEntity>?> {
        val result = kotlin.runCatching { api?.getMusicList(page, size, keyword, sort) }
        return processNetworkResult(result)
    }

    suspend fun getHistoryMusicList(page: Int, size: Int): RequestResult<ListData<MusicEntity>?> {
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
                initialLoadSize = 100
            ),
            pagingSourceFactory = { RecentMusicPagingSource(this) }
        ).flow
    }

    private suspend fun processNetworkResult(
        result: Result<BaseListResponse<MusicResult>?>,
        cacheMusic: Boolean = true
    ): RequestResult<ListData<MusicEntity>?> {
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

    suspend fun getMusicComment(musicId: String, page: Int, size: Int): RequestResult<ListData<CommentAdapter.CommentBean>?> {
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

    suspend fun getMusicCommentReply(commentId: String, page: Int, size: Int): RequestResult<ListData<CommentAdapter.CommentBean>?> {
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
    ): RequestResult<ListData<MusicComment>?> {
        val map = mutableMapOf("music_id" to musicId, "content" to content)
        if (commentId != null) {
            map["parent_id"] = commentId
        }
        val result = kotlin.runCatching { api?.postMusicComment(map) }
        return result.toRequestResult()
    }

    suspend fun likeMusicComment(commentId: String): RequestResult<Unit?> {
        val result = kotlin.runCatching { api?.likeMusicComment(commentId) }
        return result.toRequestResult()
    }

    suspend fun getSearchHotKeywords(limit: Int = 10): RequestResult<List<SearchHotKeyword>?> {
        val result = kotlin.runCatching { api?.getSearchHotKeywords("music", limit) }
        return result.toRequestResult()
    }

    suspend fun getPlaylistDetail(id: String): RequestResult<PlaylistDetailResult?> {
        val result = kotlin.runCatching { api?.getPlaylistDetail(id, 1, 10) }
        return result.toRequestResult()
    }

    /**
     * 获取播放列表
     * @param isRecommend 是否推荐列表
     */
    suspend fun getPlaylistList(isRecommend: Boolean, page: Int, limit: Int): RequestResult<List<PlaylistResult>?> {
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
                initialLoadSize = 100
            ),
            pagingSourceFactory = { PlayListPagingSource(this, isRecommend) }
        ).flow
    }

    suspend fun getPlaylistDetail(
        id: String,
        page: Int,
        size: Int,
        playlistDetail: MutableLiveData<PlaylistDetailResult>
    ): RequestResult<ListData<MusicEntity>?> {
        val result = runCatching {
            api?.getPlaylistDetail(id, page, size)?.let {
                BaseListResponse<MusicResult>().apply {
                    code = it.code
                    message = it.message
                    data = it.data?.tracks
                    success = it.success
                    if (it.success == true && playlistDetail.value == null) {
                        playlistDetail.value = it.data
                    }
                }
            }
        }
        return processNetworkResult(result)
    }

    fun getPlaylistDetailPaging(
        id: String,
        playlistDetail: MutableLiveData<PlaylistDetailResult>
    ): Flow<PagingData<MusicEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                prefetchDistance = 2,
                initialLoadSize = 100
            ),
            pagingSourceFactory = { PlayListDetailPagingSource(this, id, playlistDetail) }
        ).flow
    }

    /**
     * 创建播放列表
     */
    suspend fun createPlaylist(title: String): RequestResult<Unit?> {
        val result = kotlin.runCatching { api?.createPlaylist(mapOf("title" to title)) }
        return result.toRequestResult()
    }

    /**
     * 导入播放列表
     */
    suspend fun importPlaylist(url: String): RequestResult<Unit?> {
        val result = kotlin.runCatching { api?.importPlaylist(mapOf("url" to url, "platform" to "mobile")) }
        return result.toRequestResult()
    }

    /**
     * 更新播放列表
     */
    suspend fun updatePlaylist(playlist: PlaylistDetailResult): RequestResult<Unit?> {
        val map = mutableMapOf<String, Any>()
        playlist.title?.let { map["title"] = it }
        playlist.description?.let { map["description"] = it }
        playlist.coverImage?.let { map["cover_image"] = it }
        playlist.tags?.let { tags -> map["tags"] = tags.map { it.id } }
        playlist.isPublic?.let { map["is_public"] = it }
        val result = kotlin.runCatching { api?.updatePlaylist(playlist.id.toString(), map) }
        return result.toRequestResult()
    }
    
    /**
     * 获取音乐标签
     */
    suspend fun getMusicTags(): RequestResult<List<Tag>?> {
        val result = kotlin.runCatching { api?.getMusicTags() }
        return result.toRequestResult()
    }

    /**
     * 删除播放列表
     */
    suspend fun deletePlaylist(id: String): RequestResult<Unit?> {
        val result = kotlin.runCatching { api?.deletePlaylist(id) }
        return result.toRequestResult()
    }

    /**
     * 向播放列表添加音乐
     */
    suspend fun addMusicToPlaylist(id: String, musicIds: List<String>): RequestResult<Unit?> {
        val result =
            kotlin.runCatching { api?.addMusicToPlaylist(id, mapOf("musicIds" to musicIds)) }
        return result.toRequestResult()
    }

    /**
     * 从播放列表删除音乐
     */
    suspend fun deleteMusicInPlaylist(id: String, musicIds: List<String>): RequestResult<Unit?> {
        val result = kotlin.runCatching { api?.deleteMusicInPlaylist(id, mapOf("musicIds" to musicIds)) }
        return result.toRequestResult()
    }

    fun getArtistPaging(keyword: String, tagId: String? = null): Flow<PagingData<ArtistResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                prefetchDistance = 2,
                initialLoadSize = 100
            ),
            pagingSourceFactory = { ArtistPagingSource(this, keyword, tagId) }
        ).flow
    }

    suspend fun getArtistList(
        page: Int,
        limit: Int,
        keyword: String,
        tagId: String? = null
    ): RequestResult<ListData<ArtistResult>?> {
        val result = kotlin.runCatching { api?.getArtistList(page, limit, keyword, tagId) }
        return result.toRequestResult()
    }

    suspend fun getDashboardStats(): RequestResult<DashboardStats?>{
        return runCatching { api?.getDashBoardStats() }.toRequestResult()
    }
}