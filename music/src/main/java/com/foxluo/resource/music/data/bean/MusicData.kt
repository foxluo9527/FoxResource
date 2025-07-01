package com.foxluo.resource.music.data.bean

import com.foxluo.resource.music.player.bean.base.BaseMusicItem
import java.io.Serializable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.ForeignKey
import androidx.room.Index
import com.foxluo.baselib.util.ImageExt
import com.foxluo.resource.music.data.result.MusicResult

@Entity(
    tableName = "music",
    indices = [
        Index(value = ["music_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = ArtistData::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MusicData(
    @PrimaryKey
    @ColumnInfo(name = "music_id")
    val musicId: String,

    @ColumnInfo(name = "cover_img")
    val coverImg: String?,

    @ColumnInfo(name = "url")
    val url: String?,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "artist_id", index = true) // 改为存储外键
    var artistId: Long = 0,

    // 子类特有字段
    @ColumnInfo(name = "album_id")
    val albumId: Long? = null,

    @ColumnInfo(name = "is_collection")
    var isCollection: Boolean = false,

    @ColumnInfo(name = "lyrics")
    val lyrics: String? = null,

    @ColumnInfo(name = "lyrics_trans")
    val lyricsTrans: String? = null
) : BaseMusicItem<ArtistData>(
    musicId, coverImg, url, title, null
), Serializable {
    @Transient
    var artist: ArtistData? = null
        set(value) {
            field = value
            super.artist = value
        }
}
