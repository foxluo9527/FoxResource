package com.foxluo.resource.music.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.bean.AlbumWithDetails
import com.foxluo.resource.music.data.bean.MusicAlbumJoin
import com.foxluo.resource.music.data.bean.MusicData

@Dao
interface AlbumDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(album: AlbumData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAlbum(album: AlbumData)

    // 获取带音乐列表的专辑
    @Transaction
    @Query("SELECT * FROM albums WHERE album_id = :albumId")
    suspend fun getAlbumWithMusics(albumId: String): AlbumWithMusics?

    @Transaction
    suspend fun updateAlbumWithMusics(album: AlbumData, musicDao: MusicDAO) {
        // 1. 插入/更新专辑
        updateAlbum(album)

        val musics = album.musics ?: return

        // 2. 批量处理音乐数据
        musicDao.insertMusics(musics)

        // 3. 更新关联关系
        setAlbumMusics(album.albumId, musics.map { it.musicId })
    }

    @Transaction
    suspend fun setAlbumMusics(albumId: String, musicIds: List<String>?) {
        musicIds ?: return
        // 先清空旧关系
        clearMusicsInAlbum(albumId)

        // 插入新关系（使用 ignore 策略防止重复）
        if (musicIds.isNotEmpty()) {
            val joins = musicIds.map { musicId ->
                MusicAlbumJoin(
                    musicId = musicId,
                    albumId = albumId,
                    addTime = System.currentTimeMillis()
                )
            }
            insertMusicAlbumJoins(joins)
        }
    }

    // 私有方法用于批量插入关联关系
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMusicAlbumJoins(joins: List<MusicAlbumJoin>)

    // 保留原有清空方法（改为私有）
    @Query("DELETE FROM music_album_join WHERE album_id = :albumId")
    suspend fun clearMusicsInAlbum(albumId: String)

    @Query("DELETE FROM music_album_join WHERE album_id = :albumId AND music_id=:musicId")
    suspend fun removeMusicInAlbum(albumId: String,musicId: String)
}

data class AlbumWithMusics(
    @Embedded
    val album: AlbumData,

    @Relation(
        entity = MusicData::class,
        parentColumn = "album_id",
        entityColumn = "music_id",
        associateBy = Junction(
            value = MusicAlbumJoin::class,
            parentColumn = "album_id",
            entityColumn = "music_id"
        )
    )
    val musics: List<MusicWithArtist> // 修改为包含艺人的类型
){
    // 转换方法
    fun toAlbumData(): AlbumData = album.copy().apply {
        musics = this@AlbumWithMusics.musics.map { it.getMusicWithArtist() }
    }
}