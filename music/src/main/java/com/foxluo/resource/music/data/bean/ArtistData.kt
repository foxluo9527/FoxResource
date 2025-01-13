package com.foxluo.resource.music.data.bean

import com.foxluo.resource.music.player.bean.base.BaseArtistItem

class ArtistData : BaseArtistItem {
    constructor(name: String?) : super(name)

    var artistId: Long? = null

    var avatar: String? = null

    var alias: List<String>? = null

    var cover: String? = null

    var description: String? = null

    var isCollection: Boolean = false
}