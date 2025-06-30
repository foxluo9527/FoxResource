package com.foxluo.resource.music.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.bean.ArtistData
import com.foxluo.resource.music.data.bean.DateTimeConverter
import com.foxluo.resource.music.data.bean.ListConverter
import com.foxluo.resource.music.data.bean.MusicAlbumJoin
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.dao.AlbumDAO
import com.foxluo.resource.music.data.dao.ArtistDAO
import com.foxluo.resource.music.data.dao.MusicDAO

@Database(entities = [MusicData::class, ArtistData::class, AlbumData::class,MusicAlbumJoin::class], version = 1)
@TypeConverters(DateTimeConverter::class,ListConverter::class) // 添加类型转换器
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDAO
    abstract fun artistDao(): ArtistDAO
    abstract fun albumDao(): AlbumDAO
}