package com.foxluo.resource.music.data.domain

import android.app.Application
import androidx.room.Room
import com.blankj.utilcode.util.Utils
import com.foxluo.baselib.domain.ModuleInitializer
import com.foxluo.baselib.util.Constant
import com.foxluo.baselib.util.Constant.COLLECTION_LIST_ALBUM_TITLE
import com.foxluo.baselib.util.Constant.HISTORY_LIST_ALBUM_TITLE
import com.foxluo.baselib.util.Constant.PLAY_LIST_ALBUM_TITLE
import com.foxluo.resource.music.data.database.AlbumEntity
import com.foxluo.resource.music.data.database.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicModuleInitializer : ModuleInitializer {
    companion object {
        val musicDb by lazy {
            Room.databaseBuilder(
                Utils.getApp(),
                MusicDatabase::class.java, "fox_resource_music"
            ).build()
        }
    }

    override suspend fun onAppCreate(app: Application) {
        val albumDao = musicDb.albumDao()
        withContext(Dispatchers.IO) {
            albumDao.insertOrIgnore(
                AlbumEntity(
                    albumId = Constant.TABLE_ALBUM_PLAYING_ID.toString(),
                    title = PLAY_LIST_ALBUM_TITLE
                )
            )
            albumDao.insertOrIgnore(
                AlbumEntity(
                    albumId = Constant.TABLE_ALBUM_COLLECTION_ID.toString(),
                    title = COLLECTION_LIST_ALBUM_TITLE
                )
            )
            albumDao.insertOrIgnore(
                AlbumEntity(
                    albumId = Constant.TABLE_ALBUM_HISTORY_ID.toString(),
                    title = HISTORY_LIST_ALBUM_TITLE
                )
            )
        }
    }
}