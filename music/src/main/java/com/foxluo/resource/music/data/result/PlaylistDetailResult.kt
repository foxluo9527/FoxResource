package com.foxluo.resource.music.data.result

import com.foxluo.baselib.data.result.ListData
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 *    Author : 罗福林
 *    Date   : 2026/1/14
 *    Desc   :
 */
@Serializable
data class PlaylistDetailResult (
    val artists: List<Artist>?,

    @SerializedName("collection_count")
    val collectionCount: Long?,

    @SerializedName("cover_image")
    val coverImage: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("creator_id")
    val creatorID: Long? = null,

    val description: String? = null,
    val duration: Long?,

    @SerializedName("favorite_count")
    val favoriteCount: Long?,

    val id: Long?,

    @SerializedName("is_featured")
    val isFeatured: Boolean?,

    @SerializedName("is_public")
    val isPublic: Int?,

    val language: String? = null,

    @SerializedName("like_count")
    val likeCount: Long?,

    @SerializedName("play_count")
    val playCount: Int?,

    val publisher: String?,

    @SerializedName("release_date")
    val releaseDate: String?,

    val tags: List<Tag>? = null,

    val title: String?,

    @SerializedName("track_count")
    val trackCount: Long?,

    val tracks: ListData<MusicResult>?,
    val type: String?,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("view_count")
    val viewCount: Long?
) : java.io.Serializable

@Serializable
data class Tag (
    val id: Long,
    val name: String
) : java.io.Serializable

@Serializable
data class Artist (
    val alias: String? = null,
    val avatar: String? = null,

    @SerializedName("cover_image")
    val coverImage: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    val description: String? = null,

    @SerializedName("favorite_count")
    val favoriteCount: Long? = null,

    val gender: String? = null,
    val id: Long? = null,

    @SerializedName("is_verified")
    val isVerified: Long? = null,

    val name: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("view_count")
    val viewCount: Long? = null
) : java.io.Serializable