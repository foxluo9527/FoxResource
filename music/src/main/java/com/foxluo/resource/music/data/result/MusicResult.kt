package com.foxluo.resource.music.data.result

data class MusicResult(
    val id: Long,
    val title: String,
    val description: String?,
    val isCollection: Boolean?,
    val url: String,
    val cover_image: String?,
    val lyrics: String = "",
    val lyrics_trans: String = "",
    val album: Album? = null,
    val artists: List<MusicArtist>? = null
)

data class Album(val id: Long, val title: String, val cover_image: String?)

data class MusicArtist(
    val id: Long,
    val name: String,
    val alias: String?,
    val avatar: String?,
    val cover_image: String?,
    val description: String?
)
