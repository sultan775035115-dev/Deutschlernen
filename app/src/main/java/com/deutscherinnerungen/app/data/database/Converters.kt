package com.deutscherinnerungen.app.data.database

import androidx.room.TypeConverter
import com.deutscherinnerungen.app.data.entity.ReminderType

class Converters {
    @TypeConverter
    fun fromReminderType(value: ReminderType?): String {
        return value?.name ?: ReminderType.DAILY.name
    }

    @TypeConverter
    fun toReminderType(value: String?): ReminderType {
        return try {
            if (value != null) ReminderType.valueOf(value) else ReminderType.DAILY
        } catch (e: Exception) {
            ReminderType.DAILY
        }
    }
}
