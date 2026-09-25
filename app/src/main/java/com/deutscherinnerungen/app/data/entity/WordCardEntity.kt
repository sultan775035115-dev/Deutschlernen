package com.deutscherinnerungen.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * نوع التذكير:
 * DAILY: تذكير في ساعة ودقيقة محددة يومياً (مثال: 09:00 أو 21:30)
 * INTERVAL: تذكير يتكرر كل عدد ساعات (مثال: كل 4 ساعات)
 */
enum class ReminderType {
    @SerializedName("DAILY")
    DAILY,
    @SerializedName("INTERVAL")
    INTERVAL
}

/**
 * كيان البطاقة في قاعدة بيانات Room
 * مصمم ليكون قابلاً للتوسع لدعم مزامنة سحابية، وسوم Tags، ومستويات CEFR مستقبلاً
 */
@Entity(
    tableName = "word_cards",
    indices = [
        Index(value = ["nextReviewAt"]),
        Index(value = ["isFavorite"]),
        Index(value = ["createdAt"])
    ]
)
data class WordCardEntity(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0L,

    @SerializedName("word")
    val word: String,

    @SerializedName("meaning")
    val meaning: String,

    @SerializedName("notes")
    val notes: String = "",

    // مسارات الملفات المخزنة محلياً في مجلد التطبيق الداخلي
    @SerializedName("imagePath")
    val imagePath: String? = null,

    @SerializedName("audioPath")
    val audioPath: String? = null,

    @SerializedName("videoPath")
    val videoPath: String? = null,

    // التواريخ الزمنية بالمللي ثانية
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    // مستوى التكرار المتباعد (0 = جديدة، 1 = 1 يوم، 2 = 3 أيام، 3 = 7 أيام، 4 = 14 يوماً، 5 = 30 يوماً، 6 = 60 يوماً)
    @SerializedName("reviewLevel")
    val reviewLevel: Int = 0,

    @SerializedName("lastReviewedAt")
    val lastReviewedAt: Long? = null,

    @SerializedName("nextReviewAt")
    val nextReviewAt: Long = System.currentTimeMillis(),

    // إعدادات التذكير الخاصة بالبطاقة
    @SerializedName("reminderEnabled")
    val reminderEnabled: Boolean = false,

    @SerializedName("reminderType")
    val reminderType: ReminderType = ReminderType.DAILY,

    @SerializedName("reminderHour")
    val reminderHour: Int = 9,

    @SerializedName("reminderMinute")
    val reminderMinute: Int = 0,

    @SerializedName("reminderIntervalHours")
    val reminderIntervalHours: Int = 4,

    @SerializedName("isFavorite")
    val isFavorite: Boolean = false
)
