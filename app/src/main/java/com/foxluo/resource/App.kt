package com.foxluo.resource

import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tony.defenselib.DefenseCrash
import com.android.tony.defenselib.handler.IExceptionHandler
import com.blankj.utilcode.BuildConfig
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.foxluo.baselib.ui.BaseApplication
import com.foxluo.baselib.util.Constant
import com.foxluo.baselib.util.Constant.COLLECTION_LIST_ALBUM_TITLE
import com.foxluo.baselib.util.Constant.HISTORY_LIST_ALBUM_TITLE
import com.foxluo.baselib.util.Constant.PLAY_LIST_ALBUM_TITLE
import com.foxluo.resource.activity.CrashActivity
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.db.AppDatabase
import com.foxluo.resource.music.data.result.Album
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : BaseApplication() {
    companion object{
        val db by lazy {
            Room.databaseBuilder(
                Utils.getApp(),
                AppDatabase::class.java, "fox_resource_db"
            ).build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        // 确保 ARouter 正确初始化
        if (BuildConfig.DEBUG) {
            ARouter.openLog()     // 打印日志
            ARouter.openDebug()   // 开启调试模式
        }

        try {
            ARouter.init(this)
        } catch (e: Exception) {
            Log.e("ARouterInit", "Initialization failed", e)
        }
        val albumDao = db.albumDao()
        CoroutineScope(Dispatchers.IO).launch {
            albumDao.insertOrIgnore(
                AlbumData(
                    albumId = Constant.TABLE_ALBUM_PLAYING_ID.toString(),
                    title = PLAY_LIST_ALBUM_TITLE
                )
            )
            albumDao.insertOrIgnore(
                AlbumData(
                    albumId = Constant.TABLE_ALBUM_COLLECTION_ID.toString(),
                    title = COLLECTION_LIST_ALBUM_TITLE
                )
            )
            albumDao.insertOrIgnore(
                AlbumData(
                    albumId = Constant.TABLE_ALBUM_HISTORY_ID.toString(),
                    title = HISTORY_LIST_ALBUM_TITLE
                )
            )
        }
    }
}