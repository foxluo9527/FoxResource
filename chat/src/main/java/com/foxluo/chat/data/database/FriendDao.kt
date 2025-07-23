package com.foxluo.chat.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    // 1. 更新用户全部好友关系（事务操作）
    @Transaction
    suspend fun updateAllFriends(userId: Int, friends: List<FriendEntity>) {
        deleteByUserId(userId)
        insertAll(friends)
    }

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(friends: List<FriendEntity>)

    // 根据用户ID删除所有好友
    @Query("DELETE FROM friend WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Int)

    // 2. 获取用户全部好友数据
    @Query("SELECT * FROM friend WHERE user_id = :userId ORDER BY nickname ASC")
    fun getAllFriends(userId: Int): Flow<List<FriendEntity>>

    // 3. 删除单条好友关系
    @Delete
    suspend fun deleteFriend(friend: FriendEntity)

    // 或者通过ID删除的版本
    @Query("DELETE FROM friend WHERE id = :id")
    suspend fun deleteFriendById(id: Int)

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Query("SELECT * FROM friend WHERE id = :id")
    suspend fun getFriendById(id: Int): FriendEntity?
}