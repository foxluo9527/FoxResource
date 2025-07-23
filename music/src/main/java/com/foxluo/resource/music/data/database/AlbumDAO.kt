package com.foxluo.resource.music.data.database

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Dao
interface AlbumDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(album: AlbumEntity)

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Query("SELECT * FROM albums WHERE album_id = :albumId")
    suspend fun getAlbum(albumId: String): AlbumEntity

    @Transaction
    @Query("SELECT * FROM music_album_join WHERE album_id = :albumId ORDER BY sort ASC")
    suspend fun getMusicAlbumJoin(albumId: String): List<MAJoinWithMusicWithArtist>

    // 获取带音乐列表的专辑
    @Transaction
    suspend fun getAlbumWithMusics(albumId: String): AlbumEntity {
        return getAlbum(albumId).apply {
            musics = getMusicAlbumJoin(albumId).map {
                it.music
            }.map {
                it.getMusicWithArtist()
            }
        }
    }

    @Transaction
    suspend fun updateAlbumWithMusics(album: AlbumEntity, musicDao: MusicDAO) {
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
            val joins = musicIds.mapIndexed { index, musicId ->
                MusicAlbumJoin(
                    musicId = musicId,
                    albumId = albumId,
                    addTime = System.currentTimeMillis(),
                    sort = index
                )
            }
            insertMusicAlbumJoins(joins)
        }
    }

    @Query("DELETE FROM music_album_join WHERE album_id = :albumId")
    suspend fun clearMusicsInAlbum(albumId: String) {
        LogUtils.d(
            "DB_DEBUG",
            "[${System.currentTimeMillis()}] 清空专辑关联: albumId=$albumId 调用栈: ${Thread.currentThread().stackTrace.joinToString { it.methodName }}"
        )
        // 实际删除操作
        _clearMusicsInAlbum(albumId)
    }

    @Query("DELETE FROM music_album_join WHERE album_id = :albumId")
    suspend fun _clearMusicsInAlbum(albumId: String) // 实际执行SQL的方法

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMusicAlbumJoins(joins: List<MusicAlbumJoin>) {
        LogUtils.d(
            "DB_DEBUG",
            "[${System.currentTimeMillis()}] 插入关联关系: ${joins.joinToString { "{album=${it.albumId}, music=${it.musicId}, sort=${it.sort}}" }}"
        )
        _insertMusicAlbumJoins(joins)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun _insertMusicAlbumJoins(joins: List<MusicAlbumJoin>) // 实际执行SQL的方法

    @Query("DELETE FROM music_album_join WHERE album_id = :albumId AND music_id=:musicId")
    suspend fun removeMusicInAlbum(albumId: String, musicId: String)

    fun updateAlbumWithMusicsJava(musicAlbum: AlbumEntity, musicDao: MusicDAO) {
        CoroutineScope(Dispatchers.IO).launch(CoroutineExceptionHandler { _, throwable ->
            LogUtils.e(throwable.message)
        }) {
            updateAlbumWithMusics(musicAlbum, musicDao)
        }
    }
}

/**
 * 需要中间表的字段时使用该关联查询
 * 该查询仅通过查询出的music_album_join表信息关联查询对应的MusicData
 * 通过music_album_join的music_id对应MusicData的music_id
 * 查出的结果为MusicWithArtist，该结果会再次关联查询出对应的artist信息填充至MusicData的artist
 */
data class MAJoinWithMusicWithArtist(
    @Embedded
    val maJoin: MusicAlbumJoin,

    @Relation(
        entity = MusicEntity::class,
        parentColumn = "music_id",
        entityColumn = "music_id"
    )
    val music: MusicWithArtist
)

/**
 * 若无需使用中间表的字段使用该关联查询
 * 通过MusicAlbumJoin的album_id与AlbumData的album_id关联，
 * 对应出MusicAlbumJoin的music_id与MusicData的music_id的对应关系，查出MusicWithArtist
 * 而MusicWithArtist也是一个关联查询具体查看 MusicWithArtist类
 */
data class AlbumWithMusics(
    @Embedded
    val album: AlbumEntity,

    @Relation(
        entity = MusicEntity::class,
        parentColumn = "album_id",
        entityColumn = "music_id",
        associateBy = Junction(
            value = MusicAlbumJoin::class,
            parentColumn = "album_id",
            entityColumn = "music_id"
        )
    )
    val musics: List<MusicWithArtist>
) {
    // 转换方法
    fun toAlbumData(): AlbumEntity = album.copy().apply {
        musics = this@AlbumWithMusics.musics.map { it.getMusicWithArtist() }
    }
}