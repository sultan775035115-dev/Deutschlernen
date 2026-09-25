package com.deutscherinnerungen.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.deutscherinnerungen.app.MainActivity
import com.deutscherinnerungen.app.R

/**
 * فئة مساعدة لإنشاء وإدارة قنوات وإشعارات تذكير الكلمات
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "word_anchor_reminders_channel"
        const val EXTRA_CARD_ID = "extra_card_id"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * إظهار إشعار تذكيري لبطاقة الكلمة
     */
    fun showWordReminderNotification(cardId: Long, word: String, meaning: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.deutscherinnerungen.app.ACTION_OPEN_CARD"
            putExtra(EXTRA_CARD_ID, cardId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            cardId.toInt(),
            intent,
            pendingIntentFlag
        )

        // إجراء 1: تذكرتها (ترقية المستوى وحفظ التقدم)
        val rememberedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_REMEMBERED
            putExtra(NotificationActionReceiver.EXTRA_CARD_ID, cardId)
            putExtra(NotificationActionReceiver.EXTRA_WORD, word)
        }
        val rememberedPendingIntent = PendingIntent.getBroadcast(
            context,
            (cardId * 10 + 1).toInt(),
            rememberedIntent,
            pendingIntentFlag
        )

        // إجراء 2: نسيتها (إعادة جدولة بعد ساعة)
        val forgotIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_FORGOT
            putExtra(NotificationActionReceiver.EXTRA_CARD_ID, cardId)
            putExtra(NotificationActionReceiver.EXTRA_WORD, word)
        }
        val forgotPendingIntent = PendingIntent.getBroadcast(
            context,
            (cardId * 10 + 2).toInt(),
            forgotIntent,
            pendingIntentFlag
        )

        // إجراء 3: نطق صوتي مباشر
        val speakIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SPEAK
            putExtra(NotificationActionReceiver.EXTRA_WORD, word)
        }
        val speakPendingIntent = PendingIntent.getBroadcast(
            context,
            (cardId * 10 + 3).toInt(),
            speakIntent,
            pendingIntentFlag
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("تذكير: $word")
            .setContentText(meaning)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$word\n$meaning")
                    .setSummaryText(context.getString(R.string.app_name))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "تذكرتها 👍", rememberedPendingIntent)
            .addAction(0, "نسيتها ❌", forgotPendingIntent)
            .addAction(0, "نطق 🔊", speakPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(cardId.toInt(), notification)
        } catch (e: SecurityException) {
            // تحدث في أندرويد 13+ إذا لم يمنح المستخدم إذن POST_NOTIFICATIONS
            e.printStackTrace()
        }
    }

    /**
     * إلغاء إشعار معين لبطاقة
     */
    fun cancelNotification(cardId: Long) {
        NotificationManagerCompat.from(context).cancel(cardId.toInt())
    }

    /**
     * إلغاء جميع الإشعارات
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
