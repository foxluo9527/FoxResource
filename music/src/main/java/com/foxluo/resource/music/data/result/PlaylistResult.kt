// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json    = Json { allowStructuredMapKeys = true }
// val request = json.parse(Request.serializer(), jsonString)

package com.foxluo.resource.music.data.result

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.*

/**
 * {
 *             "id": 756,
 *             "title": "欧美流行",
 *             "cover_image": "/uploads/oss/image/1736422839778_zoZqkl.jpg",
 *             "description": "欧美流行音乐精选",
 *             "release_date": null,
 *             "type": "playlist",
 *             "language": "English",
 *             "publisher": null,
 *             "creator_id": 1,
 *             "is_public": 1,
 *             "duration": 0,
 *             "track_count": 2,
 *             "view_count": 0,
 *             "like_count": 0,
 *             "collection_count": 0,
 *             "is_featured": false,
 *             "created_at": "2026-01-09T08:28:59.000Z",
 *             "updated_at": "2026-01-09T08:29:02.000Z",
 *             "favorite_count": 0
 *         }
 */
@Serializable
data class PlaylistResult (
    @SerializedName("collection_count")
    val collectionCount: Long,

    @SerializedName("cover_image")
    val coverImage: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("creator_id")
    val creatorID: Long,

    val description: String,
    val duration: Long,

    @SerializedName("favorite_count")
    val favoriteCount: Long,

    val id: Long,

    @SerializedName("is_featured")
    val isFeatured: Boolean,

    @SerializedName("is_public")
    val isPublic: Long,

    val language: String? = null,

    @SerializedName("like_count")
    val likeCount: Long,

    val title: String,

    @SerializedName("track_count")
    val trackCount: Long,

    val type: String,

    @SerializedName("view_count")
    val viewCount: Long,
     @SerializedName("is_importing")
    val isImporting: Boolean? = null,
)