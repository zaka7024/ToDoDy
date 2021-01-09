package com.zaka7024.todody.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDate
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(value) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromString(string: String): MutableList<String> {
        val listType: Type = object : TypeToken<MutableList<String?>?>() {}.type
        return Gson().fromJson(string, listType)
    }

    @TypeConverter
    fun fromMutableList(mutableList: MutableList<String>): String {
        return Gson().toJson(mutableList)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
