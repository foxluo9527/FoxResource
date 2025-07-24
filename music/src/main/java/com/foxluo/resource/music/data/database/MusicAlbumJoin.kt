package com.foxluo.resource.music.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.foxluo.baselib.util.TimeUtil.nowTime

@Entity(
    tableName = "music_album_join",
    primaryKeys = ["music_id", "album_id"],
    foreignKeys = [
        ForeignKey(
            entity = MusicEntity::class,
            parentColumns = ["music_id"],
            childColumns = ["music_id"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["album_id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class MusicAlbumJoin(
    @ColumnInfo(name = "music_id")
    val musicId: String,

    @ColumnInfo(name = "album_id")
    val albumId: String,

    @ColumnInfo(name = "add_time")
    val addTime: Long = nowTime,

    /**
     * 插入数据库时位于列表的位置
     */
    @ColumnInfo(name = "sort")
    val sort: Int
)