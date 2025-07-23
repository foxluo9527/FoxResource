package com.foxluo.resource.music.data.result

import com.foxluo.baselib.util.ImageExt
import com.foxluo.resource.music.data.database.ArtistEntity
import com.foxluo.resource.music.data.database.MusicEntity

data class MusicResult(
    val id: Long,
    val title: String,
    val description: String?,
    val isFavorite: Boolean?,
    val url: String,
    val cover_image: String?,
    val lyrics: String = "",
    val lyrics_trans: String = "",
    val album: Album? = null,
    val artists: List<MusicArtist>? = null
){
    fun toMusicData() = MusicEntity(
        musicId = this.id.toString(),
        coverImg = this.cover_image,
        url = ImageExt.processUrl(this.url),
        title = this.title,
        lyrics = this.lyrics,
        albumId = this.album?.id,
        lyricsTrans = this.lyrics_trans,
        isCollection = this.isFavorite == true
    ).apply {
        artist = this@MusicResult.artists?.firstOrNull()?.toArtistData()
    }
}

data class Album(val id: Long, val title: String, val cover_image: String?)

data class MusicArtist(
    val id: Long,
    val name: String,
    val alias: String?,
    val avatar: String?,
    val cover_image: String?,
    val description: String?
){
    fun toArtistData() = ArtistEntity(
        artistId = this.id,
        name = this.name,
        avatar = this.avatar,
        alias = this.alias?.split(';'),
        cover = this.cover_image,
        description = this.description
    )
}
