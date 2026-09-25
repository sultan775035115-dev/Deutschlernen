package com.deutscherinnerungen.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deutscherinnerungen.app.backup.BackupManager
import com.deutscherinnerungen.app.data.repository.WordCardRepository
import com.deutscherinnerungen.app.media.MediaStorageManager
import com.deutscherinnerungen.app.notifications.NotificationHelper
import com.deutscherinnerungen.app.notifications.ReminderScheduler
import com.deutscherinnerungen.app.settings.AppSettings
import com.deutscherinnerungen.app.settings.SettingsRepository
import com.deutscherinnerungen.app.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsOperationMessage(
    val text: String,
    val isError: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val cardRepository: WordCardRepository,
    private val backupManager: BackupManager,
    private val reminderScheduler: ReminderScheduler,
    private val notificationHelper: NotificationHelper,
    private val mediaManager: MediaStorageManager
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _message = MutableStateFlow<SettingsOperationMessage?>(null)
    val message: StateFlow<SettingsOperationMessage?> = _message.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun toggleAllReminders(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllRemindersEnabled(enabled)
            if (enabled) {
                reminderScheduler.rescheduleAllActiveReminders()
                _message.value = SettingsOperationMessage("تم تفعيل جميع التذكيرات بنجاح")
            } else {
                reminderScheduler.cancelAllReminders()
                notificationHelper.cancelAllNotifications()
                _message.value = SettingsOperationMessage("تم إيقاف جميع التذكيرات وحذف الإشعارات المجدولة")
            }
        }
    }

    fun cancelAllScheduledNotifications() {
        viewModelScope.launch {
            reminderScheduler.cancelAllReminders()
            notificationHelper.cancelAllNotifications()
            _message.value = SettingsOperationMessage("تمت إزالة جميع التنبيهات المجدولة حالياً")
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.exportToJson(uri)
            result.fold(
                onSuccess = { count ->
                    _message.value = SettingsOperationMessage("تم تصدير $count بطاقة بنجاح إلى ملف JSON")
                },
                onFailure = { error ->
                    _message.value = SettingsOperationMessage("فشل تصدير البيانات: ${error.localizedMessage}", isError = true)
                }
            )
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.importFromJson(uri)
            result.fold(
                onSuccess = { count ->
                    _message.value = SettingsOperationMessage("تم استيراد $count بطاقة بنجاح")
                },
                onFailure = { error ->
                    _message.value = SettingsOperationMessage("فشل استيراد النسخة الاحتياطية: ${error.localizedMessage}", isError = true)
                }
            )
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            reminderScheduler.cancelAllReminders()
            notificationHelper.cancelAllNotifications()
            cardRepository.deleteAllCards()
            _message.value = SettingsOperationMessage("تم حذف جميع البيانات والبطاقات من التطبيق")
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
