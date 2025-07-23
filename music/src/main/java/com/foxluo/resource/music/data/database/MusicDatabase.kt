package com.foxluo.resource.music.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.foxluo.baselib.data.db.DateTimeConverter
import com.foxluo.baselib.data.db.ListConverter

@Database(entities = [MusicEntity::class, ArtistEntity::class, AlbumEntity::class,MusicAlbumJoin::class], version = 1)
@TypeConverters(DateTimeConverter::class, ListConverter::class) // 添加类型转换器
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDAO
    abstract fun artistDao(): ArtistDAO
    abstract fun albumDao(): AlbumDAO
}