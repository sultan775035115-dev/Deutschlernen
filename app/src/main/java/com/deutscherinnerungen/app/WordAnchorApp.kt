package com.deutscherinnerungen.app

import android.app.Application
import com.deutscherinnerungen.app.data.database.WordAnchorDatabase
import com.deutscherinnerungen.app.data.repository.WordCardRepository
import com.deutscherinnerungen.app.data.repository.WordCardRepositoryImpl
import com.deutscherinnerungen.app.media.MediaStorageManager
import com.deutscherinnerungen.app.notifications.NotificationHelper
import com.deutscherinnerungen.app.notifications.ReminderScheduler
import com.deutscherinnerungen.app.settings.SettingsRepository

class WordAnchorApp : Application() {

    val database by lazy { WordAnchorDatabase.getInstance(this) }
    val repository: WordCardRepository by lazy { WordCardRepositoryImpl(database.wordCardDao()) }
    val mediaManager by lazy { MediaStorageManager(this) }
    val notificationHelper by lazy { NotificationHelper(this) }
    val reminderScheduler by lazy { ReminderScheduler(this) }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        // تهيئة قنوات الإشعارات
        notificationHelper
    }
}
