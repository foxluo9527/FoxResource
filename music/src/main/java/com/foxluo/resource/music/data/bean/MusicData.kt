package com.foxluo.resource.music.data.bean

import com.foxluo.resource.music.player.bean.base.BaseMusicItem
import java.io.Serializable

class MusicData(musicId: String?, coverImg: String?, url: String?, title: String?, artist: ArtistData?) : BaseMusicItem<ArtistData>(
    musicId, coverImg, url, title, artist
), Serializable {
    var albumId: Long? = null

    var isCollection: Boolean = false

    var lyrics: String? = null

    var lyricsTrans: String? = null
}
