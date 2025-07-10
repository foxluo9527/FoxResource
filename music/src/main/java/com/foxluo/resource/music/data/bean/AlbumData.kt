package com.foxluo.resource.music.data.bean

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.foxluo.resource.music.player.bean.base.BaseAlbumItem
import java.time.LocalDateTime

@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["album_id"], unique = true)
    ]
)
data class AlbumData(
    @PrimaryKey
    @ColumnInfo(name = "album_id")
    val albumId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "summary")
    val summary: String? = null,

    @ColumnInfo(name = "artist_id")
    val artistId: String? = null, // 外键代替对象引用

    @ColumnInfo(name = "cover_img")
    val coverImg: String? = null,

    @ColumnInfo(name = "pub_time")
    @TypeConverters(DateTimeConverter::class)
    val pubTime: LocalDateTime? = null,

    @ColumnInfo(name = "current_pos")
    var curMusicId: Int = 0,
) : BaseAlbumItem<MusicData, ArtistData>(
    albumId, title, summary, null, coverImg, null
) {
    @Ignore
    var autoPlay: Boolean = true
        set(value) {
            field = value
            super.autoPlay = value
        }

    // 非持久化字段需要标记忽略
    @Transient
    var artist: ArtistData? = null
        set(value) {
            field = value
            super.artist = value
        }

    @Transient
    var musics: List<MusicData>? = null
        get() = field
        set(value) {
            field = value
            super.musics = value
        }
}

// 关联查询结果类
data class AlbumWithDetails(
    @Embedded val album: AlbumData,
    @Relation(
        parentColumn = "artist_id",
        entityColumn = "artist_id"
    )
    val artist: ArtistData?,
    @Relation(
        parentColumn = "album_id",
        entityColumn = "album_id"
    )
    val songs: List<MusicData>
) {
    fun getAlbumWithDetails() = album.apply {
        artist = this@AlbumWithDetails.artist
        musics = songs
    }
}