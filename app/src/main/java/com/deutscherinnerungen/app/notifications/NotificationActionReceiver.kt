package com.deutscherinnerungen.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.deutscherinnerungen.app.data.database.WordAnchorDatabase
import com.deutscherinnerungen.app.data.repository.WordCardRepositoryImpl
import com.deutscherinnerungen.app.srs.SpacedRepetitionEngine
import com.deutscherinnerungen.app.tts.TextToSpeechHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * مستقبل التفاعل السريع مع الإشعارات (تذكرتها / نسيتها / نطق)
 * يتيح مراجعة البطاقة وتحديث مستواها مباشرة من شريط الإشعارات دون الحاجة لفتح التطبيق
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMEMBERED = "com.deutscherinnerungen.app.ACTION_REMEMBERED"
        const val ACTION_FORGOT = "com.deutscherinnerungen.app.ACTION_FORGOT"
        const val ACTION_SPEAK = "com.deutscherinnerungen.app.ACTION_SPEAK"

        const val EXTRA_CARD_ID = "extra_card_id"
        const val EXTRA_WORD = "extra_word"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val cardId = intent.getLongExtra(EXTRA_CARD_ID, -1L)
        val word = intent.getStringExtra(EXTRA_WORD) ?: ""
        val notificationHelper = NotificationHelper(context)

        when (intent.action) {
            ACTION_REMEMBERED -> {
                if (cardId <= 0L) return
                notificationHelper.cancelNotification(cardId)

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = WordAnchorDatabase.getInstance(context)
                        val repository = WordCardRepositoryImpl(db.wordCardDao())
                        val reminderScheduler = ReminderScheduler(context)

                        val card = repository.getCardById(cardId)
                        if (card != null) {
                            val srsResult = SpacedRepetitionEngine.calculateNextReview(card.reviewLevel)
                            repository.markReviewed(
                                id = cardId,
                                nextLevel = srsResult.nextLevel,
                                nextReviewAt = srsResult.nextReviewAt
                            )

                            // تحديث الجدولة
                            reminderScheduler.scheduleReminder(
                                card.copy(
                                    reviewLevel = srsResult.nextLevel,
                                    nextReviewAt = srsResult.nextReviewAt
                                )
                            )

                            withContext(Dispatchers.Main) {
                                val message = if (srsResult.isMastered) {
                                    "رائع! تم تثبيت كلمة ($word) بنجاح كامل 🏆"
                                } else {
                                    "تم تثبيت ($word) والارتقاء إلى المستوى ${srsResult.nextLevel} 🎯"
                                }
                                Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_FORGOT -> {
                if (cardId <= 0L) return
                notificationHelper.cancelNotification(cardId)

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = WordAnchorDatabase.getInstance(context)
                        val repository = WordCardRepositoryImpl(db.wordCardDao())
                        val snoozeUntil = System.currentTimeMillis() + 60 * 60 * 1000 // بعد ساعة واحدة
                        repository.snoozeReview(cardId, snoozeUntil)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context.applicationContext,
                                "تمت إعادة جدولة ($word) للمراجعة بعد ساعة ⏳",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_SPEAK -> {
                if (word.isNotBlank()) {
                    val tts = TextToSpeechHelper(context.applicationContext)
                    Handler(Looper.getMainLooper()).postDelayed({
                        tts.speak(word)
                    }, 250)
                }
            }
        }
    }
}
