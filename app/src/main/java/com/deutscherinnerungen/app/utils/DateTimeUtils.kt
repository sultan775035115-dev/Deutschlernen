package com.deutscherinnerungen.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    private val arabicLocale = Locale("ar")

    fun formatDate(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", arabicLocale)
        return sdf.format(Date(timestampMillis))
    }

    fun formatDateTime(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy، hh:mm a", arabicLocale)
        return sdf.format(Date(timestampMillis))
    }

    fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val sdf = SimpleDateFormat("hh:mm a", arabicLocale)
        return sdf.format(calendar.time)
    }

    /**
     * وصف نسبي باللغة العربية لوقت المراجعة القادم
     */
    fun formatRelativeTime(targetMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = targetMillis - now

        if (diff <= 0) {
            return "مستحقة الآن"
        }

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 60 -> "بعد $minutes دقيقة"
            hours < 24 -> "بعد $hours ساعة"
            days == 1L -> "غداً"
            days == 2L -> "بعد يومين"
            days in 3L..10L -> "بعد $days أيام"
            else -> "بعد $days يوماً"
        }
    }
}
