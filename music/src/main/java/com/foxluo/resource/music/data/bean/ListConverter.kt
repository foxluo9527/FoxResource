package com.foxluo.resource.music.data.bean

import androidx.room.TypeConverter

class ListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.split(";")?.map { it.trim() }
    }

    @TypeConverter
    fun toString(list: List<String>?): String? {
        return list?.joinToString(";")
    }
}