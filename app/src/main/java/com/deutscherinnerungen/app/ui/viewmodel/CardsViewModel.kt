package com.deutscherinnerungen.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import com.deutscherinnerungen.app.data.repository.WordCardRepository
import com.deutscherinnerungen.app.media.MediaStorageManager
import com.deutscherinnerungen.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CardFilter {
    ALL,
    FAVORITES,
    DUE_TODAY
}

enum class CardSortOrder {
    NEXT_REVIEW,
    DATE_ADDED_DESC,
    DATE_ADDED_ASC
}

data class CardsUiState(
    val cards: List<WordCardEntity> = emptyList(),
    val searchQuery: String = "",
    val filter: CardFilter = CardFilter.ALL,
    val sortOrder: CardSortOrder = CardSortOrder.NEXT_REVIEW,
    val dueCardsCount: Int = 0,
    val isLoading: Boolean = false
)

class CardsViewModel(
    private val repository: WordCardRepository,
    private val mediaManager: MediaStorageManager,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(CardFilter.ALL)
    private val _sortOrder = MutableStateFlow(CardSortOrder.NEXT_REVIEW)

    val uiState: StateFlow<CardsUiState> = combine(
        repository.getAllCards(),
        _searchQuery,
        _filter,
        _sortOrder
    ) { allCards, query, filter, sortOrder ->
        val now = System.currentTimeMillis()
        val dueCount = allCards.count { it.nextReviewAt <= now }

        var filtered = allCards

        // تصفية حسب البحث
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.word.contains(query, ignoreCase = true) ||
                it.meaning.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true)
            }
        }

        // تصفية حسب التصنيف المختار
        filtered = when (filter) {
            CardFilter.ALL -> filtered
            CardFilter.FAVORITES -> filtered.filter { it.isFavorite }
            CardFilter.DUE_TODAY -> filtered.filter { it.nextReviewAt <= now }
        }

        // الترتيب
        val sorted = when (sortOrder) {
            CardSortOrder.NEXT_REVIEW -> filtered.sortedBy { it.nextReviewAt }
            CardSortOrder.DATE_ADDED_DESC -> filtered.sortedByDescending { it.createdAt }
            CardSortOrder.DATE_ADDED_ASC -> filtered.sortedBy { it.createdAt }
        }

        CardsUiState(
            cards = sorted,
            searchQuery = query,
            filter = filter,
            sortOrder = sortOrder,
            dueCardsCount = dueCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CardsUiState(isLoading = true)
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterChange(newFilter: CardFilter) {
        _filter.value = newFilter
    }

    fun onSortOrderChange(newSortOrder: CardSortOrder) {
        _sortOrder.value = newSortOrder
    }

    fun toggleFavorite(card: WordCardEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(card.id, card.isFavorite)
        }
    }

    fun deleteCard(card: WordCardEntity) {
        viewModelScope.launch {
            // إلغاء أي تذكير نشط
            reminderScheduler.cancelReminder(card.id)
            // حذف ملفات الوسائط الداخلية المرتبطة بالبطاقة لمنع الملفات اليتيمة
            mediaManager.deleteCardMedia(card.imagePath, card.audioPath, card.videoPath)
            // حذف الكيان من قاعدة البيانات
            repository.deleteCard(card)
        }
    }
}
