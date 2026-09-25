package com.deutscherinnerungen.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deutscherinnerungen.app.data.entity.ReminderType
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import com.deutscherinnerungen.app.data.repository.WordCardRepository
import com.deutscherinnerungen.app.media.MediaStorageManager
import com.deutscherinnerungen.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditCardUiState(
    val word: String = "",
    val meaning: String = "",
    val notes: String = "",
    val imagePath: String? = null,
    val audioPath: String? = null,
    val videoPath: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderType: ReminderType = ReminderType.DAILY,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val reminderIntervalHours: Int = 4,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class AddEditCardViewModel(
    private val cardId: Long?,
    private val repository: WordCardRepository,
    private val mediaManager: MediaStorageManager,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCardUiState(isEditMode = cardId != null && cardId > 0))
    val uiState: StateFlow<AddEditCardUiState> = _uiState.asStateFlow()

    private var existingCard: WordCardEntity? = null

    init {
        if (cardId != null && cardId > 0) {
            loadExistingCard(cardId)
        }
    }

    private fun loadExistingCard(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val card = repository.getCardById(id)
            if (card != null) {
                existingCard = card
                _uiState.value = _uiState.value.copy(
                    word = card.word,
                    meaning = card.meaning,
                    notes = card.notes,
                    imagePath = card.imagePath,
                    audioPath = card.audioPath,
                    videoPath = card.videoPath,
                    reminderEnabled = card.reminderEnabled,
                    reminderType = card.reminderType,
                    reminderHour = card.reminderHour,
                    reminderMinute = card.reminderMinute,
                    reminderIntervalHours = card.reminderIntervalHours,
                    isEditMode = true,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "تعذر العثور على البطاقة"
                )
            }
        }
    }

    fun onWordChange(value: String) {
        _uiState.value = _uiState.value.copy(word = value, errorMessage = null)
    }

    fun onMeaningChange(value: String) {
        _uiState.value = _uiState.value.copy(meaning = value, errorMessage = null)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun onReminderToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(reminderEnabled = enabled)
    }

    fun onReminderTypeChange(type: ReminderType) {
        _uiState.value = _uiState.value.copy(reminderType = type)
    }

    fun onReminderTimeChange(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
    }

    fun onReminderIntervalChange(intervalHours: Int) {
        _uiState.value = _uiState.value.copy(reminderIntervalHours = intervalHours)
    }

    fun attachMedia(uri: Uri, type: MediaStorageManager.MediaType) {
        viewModelScope.launch {
            val copiedPath = mediaManager.copyUriToInternalStorage(uri, type)
            if (copiedPath != null) {
                when (type) {
                    MediaStorageManager.MediaType.IMAGE -> {
                        mediaManager.deleteInternalFile(_uiState.value.imagePath)
                        _uiState.value = _uiState.value.copy(imagePath = copiedPath)
                    }
                    MediaStorageManager.MediaType.AUDIO -> {
                        mediaManager.deleteInternalFile(_uiState.value.audioPath)
                        _uiState.value = _uiState.value.copy(audioPath = copiedPath)
                    }
                    MediaStorageManager.MediaType.VIDEO -> {
                        mediaManager.deleteInternalFile(_uiState.value.videoPath)
                        _uiState.value = _uiState.value.copy(videoPath = copiedPath)
                    }
                }
            }
        }
    }

    fun removeMedia(type: MediaStorageManager.MediaType) {
        viewModelScope.launch {
            when (type) {
                MediaStorageManager.MediaType.IMAGE -> {
                    mediaManager.deleteInternalFile(_uiState.value.imagePath)
                    _uiState.value = _uiState.value.copy(imagePath = null)
                }
                MediaStorageManager.MediaType.AUDIO -> {
                    mediaManager.deleteInternalFile(_uiState.value.audioPath)
                    _uiState.value = _uiState.value.copy(audioPath = null)
                }
                MediaStorageManager.MediaType.VIDEO -> {
                    mediaManager.deleteInternalFile(_uiState.value.videoPath)
                    _uiState.value = _uiState.value.copy(videoPath = null)
                }
            }
        }
    }

    fun saveCard() {
        val word = _uiState.value.word.trim()
        val meaning = _uiState.value.meaning.trim()

        if (word.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "يرجى إدخال الكلمة أو العبارة")
            return
        }

        if (meaning.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "يرجى إدخال المعنى أو الترجمة")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val currentState = _uiState.value
            val isEditing = existingCard != null

            val cardToSave = if (isEditing) {
                existingCard!!.copy(
                    word = word,
                    meaning = meaning,
                    notes = currentState.notes.trim(),
                    imagePath = currentState.imagePath,
                    audioPath = currentState.audioPath,
                    videoPath = currentState.videoPath,
                    reminderEnabled = currentState.reminderEnabled,
                    reminderType = currentState.reminderType,
                    reminderHour = currentState.reminderHour,
                    reminderMinute = currentState.reminderMinute,
                    reminderIntervalHours = currentState.reminderIntervalHours,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                WordCardEntity(
                    word = word,
                    meaning = meaning,
                    notes = currentState.notes.trim(),
                    imagePath = currentState.imagePath,
                    audioPath = currentState.audioPath,
                    videoPath = currentState.videoPath,
                    reminderEnabled = currentState.reminderEnabled,
                    reminderType = currentState.reminderType,
                    reminderHour = currentState.reminderHour,
                    reminderMinute = currentState.reminderMinute,
                    reminderIntervalHours = currentState.reminderIntervalHours,
                    reviewLevel = 0,
                    nextReviewAt = System.currentTimeMillis()
                )
            }

            val savedId = if (isEditing) {
                repository.updateCard(cardToSave)
                cardToSave.id
            } else {
                repository.insertCard(cardToSave)
            }

            val finalCard = cardToSave.copy(id = savedId)

            // جدولة التذكير الدقيق أو إلغاؤه فور الحفظ
            if (finalCard.reminderEnabled) {
                reminderScheduler.scheduleReminder(finalCard)
            } else {
                reminderScheduler.cancelReminder(finalCard.id)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSaved = true
            )
        }
    }
}
