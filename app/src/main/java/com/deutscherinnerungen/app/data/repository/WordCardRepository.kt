package com.deutscherinnerungen.app.data.repository

import com.deutscherinnerungen.app.data.entity.WordCardEntity
import kotlinx.coroutines.flow.Flow

interface WordCardRepository {
    fun getAllCards(): Flow<List<WordCardEntity>>
    suspend fun getAllCardsList(): List<WordCardEntity>
    suspend fun getCardById(id: Long): WordCardEntity?
    fun getCardByIdFlow(id: Long): Flow<WordCardEntity?>
    fun searchCards(query: String): Flow<List<WordCardEntity>>
    fun getFavoriteCards(): Flow<List<WordCardEntity>>
    fun getDueCards(currentTime: Long = System.currentTimeMillis()): Flow<List<WordCardEntity>>
    suspend fun getCardsWithReminders(): List<WordCardEntity>
    suspend fun insertCard(card: WordCardEntity): Long
    suspend fun insertCards(cards: List<WordCardEntity>): List<Long>
    suspend fun updateCard(card: WordCardEntity)
    suspend fun deleteCard(card: WordCardEntity)
    suspend fun deleteCardById(id: Long)
    suspend fun deleteAllCards()
    suspend fun toggleFavorite(id: Long, currentStatus: Boolean)
    suspend fun markReviewed(id: Long, nextLevel: Int, nextReviewAt: Long)
    suspend fun snoozeReview(id: Long, snoozeUntil: Long)
}
