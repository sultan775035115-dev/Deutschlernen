package com.deutscherinnerungen.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.deutscherinnerungen.app.data.dao.WordCardDao
import com.deutscherinnerungen.app.data.entity.WordCardEntity

@Database(
    entities = [WordCardEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WordAnchorDatabase : RoomDatabase() {

    abstract fun wordCardDao(): WordCardDao

    companion object {
        private const val DATABASE_NAME = "word_anchor.db"

        @Volatile
        private var INSTANCE: WordAnchorDatabase? = null

        fun getInstance(context: Context): WordAnchorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordAnchorDatabase::class.java,
                    DATABASE_NAME
                )
                    // عدم حذف قاعدة البيانات عند التحديث وحماية بيانات المستخدم
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
