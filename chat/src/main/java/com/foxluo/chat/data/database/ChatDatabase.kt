package com.foxluo.chat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.foxluo.baselib.data.db.DateTimeConverter
import com.foxluo.baselib.data.db.ListConverter

@Database(
    entities = [FriendEntity::class, ChatEntity::class, MessageEntity::class],
    version = 5
)
@TypeConverters(DateTimeConverter::class, ListConverter::class) // 添加类型转换器
abstract class ChatDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
}