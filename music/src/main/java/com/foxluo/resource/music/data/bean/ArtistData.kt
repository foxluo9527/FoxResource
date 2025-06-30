package com.foxluo.resource.music.data.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.foxluo.resource.music.player.bean.base.BaseArtistItem

@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["artist_id"], unique = true)
    ]
)
data class ArtistData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0, // 使用自增主键

    @ColumnInfo(name = "artist_id")
    val artistId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "avatar")
    val avatar: String? = null,

    @ColumnInfo(name = "alias")
    @TypeConverters(ListConverter::class)
    var alias: List<String>? = null,

    @ColumnInfo(name = "cover")
    var cover: String? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "isCollection")
    var isCollection: Boolean = false
) : BaseArtistItem(name)