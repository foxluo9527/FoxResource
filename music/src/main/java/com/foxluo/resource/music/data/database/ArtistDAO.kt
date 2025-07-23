package com.foxluo.resource.music.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ArtistDAO {
    // 插入艺术家（使用IGNORE策略避免重复）
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtist(artist: ArtistEntity): Long

    // 批量插入艺术家（使用IGNORE策略避免重复）
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtists(artists: List<ArtistEntity>): List<Long>

    @Query("SELECT artist_id FROM artists WHERE name = :name LIMIT 1")
    suspend fun getArtistIdByName(name: String): Long?
}