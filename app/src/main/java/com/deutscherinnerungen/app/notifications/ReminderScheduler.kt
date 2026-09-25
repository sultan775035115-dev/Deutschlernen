package com.deutscherinnerungen.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.deutscherinnerungen.app.data.database.WordAnchorDatabase
import com.deutscherinnerungen.app.data.entity.ReminderType
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * فئة مسؤولة عن جدولة التذكيرات الدقيقة باستخدام AlarmManager
 * متوافقة مع متطلبات أندرويد 12 و13 و14 فيما يخص Exact Alarms
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val ACTION_WORD_REMINDER = "com.deutscherinnerungen.app.ACTION_WORD_REMINDER"
        const val EXTRA_CARD_ID = "card_id"
    }

    /**
     * التحقق مما إذا كان التطبيق يمتلك صلاحية جدولة التنبيهات الدقيقة في أندرويد 12+
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * جدولة تذكير لبطاقة محددة
     */
    fun scheduleReminder(card: WordCardEntity) {
        if (!card.reminderEnabled) {
            cancelReminder(card.id)
            return
        }

        val triggerTime = calculateNextTriggerTime(card)
        val pendingIntent = createAlarmPendingIntent(card.id)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * إلغاء تذكير لبطاقة محددة
     */
    fun cancelReminder(cardId: Long) {
        val pendingIntent = createAlarmPendingIntent(cardId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * إلغاء جميع التذكيرات المجدولة لجميع البطاقات
     */
    suspend fun cancelAllReminders() = withContext(Dispatchers.IO) {
        val db = WordAnchorDatabase.getInstance(context)
        val cards = db.wordCardDao().getCardsWithActiveReminders()
        cards.forEach { card ->
            cancelReminder(card.id)
        }
    }

    /**
     * إعادة جدولة جميع التذكيرات النشطة (يتم استدعاؤها بعد إعادة التشغيل أو تغيير الوقت)
     */
    suspend fun rescheduleAllActiveReminders() = withContext(Dispatchers.IO) {
        val db = WordAnchorDatabase.getInstance(context)
        val cards = db.wordCardDao().getCardsWithActiveReminders()
        cards.forEach { card ->
            scheduleReminder(card)
        }
    }

    /**
     * حساب وقت التشغيل القادم للتذكير
     */
    fun calculateNextTriggerTime(card: WordCardEntity): Long {
        val now = Calendar.getInstance()

        return when (card.reminderType) {
            ReminderType.DAILY -> {
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, card.reminderHour)
                    set(Calendar.MINUTE, card.reminderMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // إذا مضى وقت اليوم، اضبطه على نفس الوقت غداً
                if (target.before(now) || target.timeInMillis <= System.currentTimeMillis()) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                target.timeInMillis
            }

            ReminderType.INTERVAL -> {
                val intervalHours = if (card.reminderIntervalHours > 0) card.reminderIntervalHours else 4
                System.currentTimeMillis() + (intervalHours * 3600 * 1000L)
            }
        }
    }

    private fun createAlarmPendingIntent(cardId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_WORD_REMINDER
            putExtra(EXTRA_CARD_ID, cardId)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(
            context,
            cardId.toInt(),
            intent,
            flags
        )
    }
}
