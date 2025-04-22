package com.foxluo.resource.music.data.repo

import com.foxluo.baselib.data.respository.BASE_URL
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.resource.music.data.api.MusicApi
import com.foxluo.resource.music.data.bean.ArtistData
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.result.toCommentList
import com.foxluo.resource.music.data.result.toCommentReplay

class MusicRepository : BaseRepository() {
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
        val dataResult = ListData<MusicData>()
        val dataList = result?.data?.let { data ->
            data.list?.map {
                MusicData(
                    it.id.toString(),
                    it.cover_image,
                    BASE_URL + it.url,
                    it.title,
                    it.artists?.firstOrNull()?.let { artist ->
                        ArtistData(artist.name).apply {
                            artistId = artist.id
                            avatar = artist.avatar
                            alias = artist.alias?.split(';')
                            cover = artist.cover_image
                            description = artist.description
                        }
                    }).apply {
                    lyrics = it.lyrics
                    albumId = it.album?.id
                    lyricsTrans = it.lyrics_trans
                }.apply {
                    isCollection = it.isFavorite == true
                }
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
            RequestResult.Success<ListData<MusicData>?>(dataResult, result.message)
        } else {
            RequestResult.Error(result?.message?:"网络连接错误")
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