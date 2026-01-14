package com.foxluo.resource.music.data.result

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 *    Author : 罗福林
 *    Date   : 2026/1/14
 *    Desc   :
 */

@Serializable
data class ArtistResult(
    @SerializedName("album_count")
    val albumCount: Long,

    val alias: String? = null,
    val avatar: String,

    @SerializedName("cover_image")
    val coverImage: String,

    @SerializedName("created_at")
    val createdAt: String,

    val description: String,

    @SerializedName("favorite_count")
    val favoriteCount: Long,

    val gender: String,
    val id: Long,

    @SerializedName("is_verified")
    val isVerified: Long,

    val isFavorite: Boolean,

    @SerializedName("music_count")
    val musicCount: Long,

    val name: String,
    val region: String? = null,
    val tags: List<String>,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("view_count")
    val viewCount: Long
)
