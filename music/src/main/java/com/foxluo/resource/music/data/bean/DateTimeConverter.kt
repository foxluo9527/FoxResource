package com.foxluo.resource.music.data.bean

import androidx.room.TypeConverter
import java.time.LocalDateTime

class DateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }
}