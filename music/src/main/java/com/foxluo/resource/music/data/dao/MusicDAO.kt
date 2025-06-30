package com.foxluo.resource.music.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.foxluo.resource.music.data.bean.ArtistData
import com.foxluo.resource.music.data.bean.MusicData

@Dao
interface MusicDAO {
    @Transaction
    suspend fun insertMusic(music: MusicData): Long {
        music.artist?.let { artist ->
            // 插入或更新艺人
            upsertArtist(artist)
            // 同步artistId到music
            music.artistId = artist.artistId
        }
        return _insertMusic(music)
    }

    // 批量插入（自动处理艺人）
    @Transaction
    suspend fun insertMusics(musics: List<MusicData>): List<Long> {
        // 提取并去重艺人
        val artists = musics.mapNotNull { it.artist }
            .distinctBy { it.artistId }

        // 批量插入艺人
        upsertArtists(artists)

        // 同步artistId到music
        musics.forEach { music ->
            music.artist?.let {
                music.artistId = it.artistId
            }
        }

        return _insertMusics(musics)
    }

    // 内部插入方法（实际执行数据库操作）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertMusic(music: MusicData): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertMusics(musics: List<MusicData>): List<Long>

    // 艺人操作方法
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArtist(artist: ArtistData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArtists(artists: List<ArtistData>)

    // 根据业务ID查询
    @Query("SELECT * FROM music WHERE music_id IN (:musicIds)")
    suspend fun getMusicsByIds(musicIds: List<String>): List<MusicData>

    @Query(
        """
        SELECT * FROM music LIMIT :size OFFSET :page * :size
    """
    )
    suspend fun getMusics(page: Int, size: Int): List<MusicWithArtist>

    @Query(
        """
        SELECT * FROM music WHERE album_id = :albumId LIMIT :size OFFSET :page * :size
    """
    )
    suspend fun getMusics(albumId: Long, page: Int, size: Int): List<MusicWithArtist>

    @Query(
        """
        SELECT * FROM music 
        WHERE title LIKE '%' || :keyword || '%' 
        ORDER BY id ASC 
        LIMIT :size OFFSET (:page - 1) * :size
    """
    )
    suspend fun searchMusics(page: Int, size: Int, keyword: String): List<MusicWithArtist>
}

// 定义关联结果类
data class MusicWithArtist(
    @Embedded val music: MusicData,
    @Relation(
        parentColumn = "artist_id",
        entityColumn = "artist_id"
    )
    val artist: ArtistData
) {
    fun getMusicWithArtist() = music.apply {
        artist = this@MusicWithArtist.artist
    }
}