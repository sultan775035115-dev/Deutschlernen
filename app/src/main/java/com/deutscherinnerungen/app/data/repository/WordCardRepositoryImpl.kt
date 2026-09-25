package com.deutscherinnerungen.app.data.repository

import com.deutscherinnerungen.app.data.dao.WordCardDao
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class WordCardRepositoryImpl(
    private val dao: WordCardDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WordCardRepository {

    override fun getAllCards(): Flow<List<WordCardEntity>> {
        return dao.getAllCardsFlow().flowOn(ioDispatcher)
    }

    override suspend fun getAllCardsList(): List<WordCardEntity> {
        return withContext(ioDispatcher) {
            dao.getAllCards()
        }
    }

    override suspend fun getCardById(id: Long): WordCardEntity? {
        return withContext(ioDispatcher) {
            dao.getCardById(id)
        }
    }

    override fun getCardByIdFlow(id: Long): Flow<WordCardEntity?> {
        return dao.getCardByIdFlow(id).flowOn(ioDispatcher)
    }

    override fun searchCards(query: String): Flow<List<WordCardEntity>> {
        return dao.searchCardsFlow(query).flowOn(ioDispatcher)
    }

    override fun getFavoriteCards(): Flow<List<WordCardEntity>> {
        return dao.getFavoriteCardsFlow().flowOn(ioDispatcher)
    }

    override fun getDueCards(currentTime: Long): Flow<List<WordCardEntity>> {
        return dao.getCardsDueForReviewFlow(currentTime).flowOn(ioDispatcher)
    }

    override suspend fun getCardsWithReminders(): List<WordCardEntity> {
        return withContext(ioDispatcher) {
            dao.getCardsWithActiveReminders()
        }
    }

    override suspend fun insertCard(card: WordCardEntity): Long {
        return withContext(ioDispatcher) {
            dao.insertCard(card)
        }
    }

    override suspend fun insertCards(cards: List<WordCardEntity>): List<Long> {
        return withContext(ioDispatcher) {
            dao.insertCards(cards)
        }
    }

    override suspend fun updateCard(card: WordCardEntity) {
        withContext(ioDispatcher) {
            dao.updateCard(card)
        }
    }

    override suspend fun deleteCard(card: WordCardEntity) {
        withContext(ioDispatcher) {
            dao.deleteCard(card)
        }
    }

    override suspend fun deleteCardById(id: Long) {
        withContext(ioDispatcher) {
            dao.deleteCardById(id)
        }
    }

    override suspend fun deleteAllCards() {
        withContext(ioDispatcher) {
            dao.deleteAllCards()
        }
    }

    override suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        withContext(ioDispatcher) {
            dao.updateFavoriteStatus(id, !currentStatus, System.currentTimeMillis())
        }
    }

    override suspend fun markReviewed(id: Long, nextLevel: Int, nextReviewAt: Long) {
        withContext(ioDispatcher) {
            dao.updateReviewProgress(
                id = id,
                level = nextLevel,
                lastReviewedAt = System.currentTimeMillis(),
                nextReviewAt = nextReviewAt,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun snoozeReview(id: Long, snoozeUntil: Long) {
        withContext(ioDispatcher) {
            dao.updateNextReviewTime(id, snoozeUntil, System.currentTimeMillis())
        }
    }
}
