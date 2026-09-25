package com.deutscherinnerungen.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deutscherinnerungen.app.data.database.WordAnchorDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * مستقبل تنبيهات AlarmManager
 * يستقبل الحدث ويعرض الإشعار حتى لو كان التطبيق مغلقاً تماماً
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_WORD_REMINDER) return

        val cardId = intent.getLongExtra(ReminderScheduler.EXTRA_CARD_ID, -1L)
        if (cardId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = WordAnchorDatabase.getInstance(context)
                val card = db.wordCardDao().getCardById(cardId)

                if (card != null && card.reminderEnabled) {
                    val notificationHelper = NotificationHelper(context)
                    notificationHelper.showWordReminderNotification(
                        cardId = card.id,
                        word = card.word,
                        meaning = card.meaning
                    )

                    // جدولة التكرار التالي (سواء كان يومياً أو كل X ساعات)
                    val scheduler = ReminderScheduler(context)
                    scheduler.scheduleReminder(card)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
