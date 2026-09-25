package com.deutscherinnerungen.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import com.deutscherinnerungen.app.data.repository.WordCardRepository
import com.deutscherinnerungen.app.media.MediaStorageManager
import com.deutscherinnerungen.app.notifications.ReminderScheduler
import com.deutscherinnerungen.app.srs.SpacedRepetitionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CardDetailUiState(
    val card: WordCardEntity? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val isAudioPlaying: Boolean = false,
    val reviewSuccessMessage: String? = null
)

class CardDetailViewModel(
    private val cardId: Long,
    private val repository: WordCardRepository,
    private val mediaManager: MediaStorageManager,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    init {
        loadCard()
    }

    private fun loadCard() {
        viewModelScope.launch {
            repository.getCardByIdFlow(cardId).collect { card ->
                _uiState.value = _uiState.value.copy(
                    card = card,
                    isLoading = false
                )
            }
        }
    }

    fun markReviewed() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            val result = SpacedRepetitionEngine.calculateNextReview(currentCard.reviewLevel)
            repository.markReviewed(
                id = currentCard.id,
                nextLevel = result.nextLevel,
                nextReviewAt = result.nextReviewAt
            )
            _uiState.value = _uiState.value.copy(
                reviewSuccessMessage = if (result.isMastered) "أحسنت! تم تثبيت هذه الكلمة بنجاح!" else "تمت المراجعة! الانتقال إلى المستوى ${result.nextLevel}"
            )
        }
    }

    fun snooze(option: SpacedRepetitionEngine.SnoozeOption, customTimeMillis: Long? = null) {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            val snoozeUntil = SpacedRepetitionEngine.calculateSnoozeTime(option, customTimeMillis)
            repository.snoozeReview(currentCard.id, snoozeUntil)
            _uiState.value = _uiState.value.copy(
                reviewSuccessMessage = "تم تأجيل موعد المراجعة"
            )
        }
    }

    fun toggleFavorite() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            repository.toggleFavorite(currentCard.id, currentCard.isFavorite)
        }
    }

    fun deleteCard() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            reminderScheduler.cancelReminder(currentCard.id)
            mediaManager.deleteCardMedia(currentCard.imagePath, currentCard.audioPath, currentCard.videoPath)
            repository.deleteCard(currentCard)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(reviewSuccessMessage = null)
    }
}
