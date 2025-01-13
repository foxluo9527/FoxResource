package com.foxluo.resource.music.data.bean

import com.foxluo.resource.music.player.bean.base.BaseMusicItem

class MusicData(musicId: String?, coverImg: String?, url: String?, title: String?, artist: ArtistData?) : BaseMusicItem<ArtistData>(
    musicId, coverImg, url, title, artist
) {
    var albumId: Long? = null

    var isCollection: Boolean = false

    var lyrics: String? = null

    var lyricsTrans: String? = null
}
