package com.foxluo.resource.music.data.repo

import com.foxluo.baselib.data.respository.BASE_URL
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.resource.music.data.api.MusicApi
import com.foxluo.resource.music.data.bean.ArtistData
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.result.toCommentList
import com.foxluo.resource.music.data.result.toCommentReplay

class MusicRepository : BaseRepository() {
    private val api by lazy {
        getApi<MusicApi>()
    }

    suspend fun favoriteMusic(id: String): RequestResult {
        val result = kotlin.runCatching { api?.favoriteMusic(id) }.getOrNull()
        return if (result?.success == true) {
            RequestResult.Success<Unit>(Unit, result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun recordPlay(id: String): RequestResult {
        val result = kotlin.runCatching { api?.recordMusicPlay(id) }.getOrNull()
        return if (result?.success == true) {
            RequestResult.Success<Unit>(Unit, result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }
    suspend fun getMusicList(page: Int, size: Int, keyword: String = ""): RequestResult {
        val result = kotlin.runCatching { api?.getMusicList(page, size, keyword) }.getOrNull()
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
            }
        }
        return if (dataList != null) {
            RequestResult.Success<List<MusicData>?>(dataList,result.message)
        } else {
            RequestResult.Error(result?.message?:"网络连接错误")
        }
    }

    suspend fun getMusicComment(musicId: String, page: Int, size: Int): RequestResult {
        val result = kotlin.runCatching { api?.getMusicComments(musicId, page, size) }.getOrNull()
        val dataList = result?.data?.list?.map {
            it.toCommentList()
        }?.flatten()
        return if (dataList != null) {
            RequestResult.Success<List<CommentAdapter.CommentBean>?>(dataList, result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun getMusicCommentReply(commentId: String, page: Int, size: Int): RequestResult {
        val result = kotlin.runCatching { api?.getMusicReplies(commentId, page, size) }.getOrNull()
        val dataList = result?.data?.let { data ->
            data.list?.map {
                it.toCommentReplay(data.current * data.pageSize >= data.total)
            }
        }
        return if (dataList != null) {
            RequestResult.Success<List<CommentAdapter.CommentBean>?>(dataList, result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }

    suspend fun postMusicComment(
        musicId: String,
        commentId: String,
        content: String
    ): RequestResult {
        val map = mapOf("music_id" to musicId, "content" to content, "parent_id" to commentId)
        val result = kotlin.runCatching { api?.postMusicComment(map) }.getOrNull()
        return if (result?.success == true) {
            RequestResult.Success<Unit>(Unit, result.message)
        } else {
            RequestResult.Error(result?.message ?: "网络连接错误")
        }
    }
}