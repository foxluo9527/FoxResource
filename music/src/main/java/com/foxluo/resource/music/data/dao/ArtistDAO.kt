package com.foxluo.resource.music.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.foxluo.resource.music.data.bean.ArtistData

@Dao
interface ArtistDAO {
    // 插入艺术家（使用IGNORE策略避免重复）
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtist(artist: ArtistData): Long

    // 批量插入艺术家（使用IGNORE策略避免重复）
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtists(artists: List<ArtistData>): List<Long>

    @Query("SELECT artist_id FROM artists WHERE name = :name LIMIT 1")
    suspend fun getArtistIdByName(name: String): Long?
}