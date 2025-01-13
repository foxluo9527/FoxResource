package com.foxluo.resource.music.data.bean

import com.foxluo.resource.music.player.bean.base.BaseAlbumItem
import java.time.LocalDateTime

class AlbumData(albumId: String?, title: String?, summary: String?, artist: ArtistData?, coverImg: String?, musics: MutableList<MusicData>?) :
    BaseAlbumItem<MusicData, ArtistData>(albumId, title, summary, artist, coverImg, musics) {
    var pubTime: LocalDateTime? = null
}