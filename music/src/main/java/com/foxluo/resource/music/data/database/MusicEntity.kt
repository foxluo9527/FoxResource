package com.foxluo.resource.music.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.foxluo.resource.music.player.bean.base.BaseMusicItem
import java.io.Serializable

@Entity(
    tableName = "music",
    indices = [
        Index(value = ["music_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class MusicEntity(
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
    val lyricsTrans: String? = null,
) : BaseMusicItem<ArtistEntity>(
    musicId, coverImg, url, title, null
), Serializable {
    @Transient
    var artist: ArtistEntity? = null
        set(value) {
            field = value
            super.artist = value
        }

    @Ignore
    var progress:Int = 0

    @Ignore
    var isSelect:Boolean = false
}