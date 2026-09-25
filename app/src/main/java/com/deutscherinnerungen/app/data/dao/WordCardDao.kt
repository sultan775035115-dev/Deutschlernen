package com.deutscherinnerungen.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCardDao {

    @Query("SELECT * FROM word_cards ORDER BY createdAt DESC")
    fun getAllCardsFlow(): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards ORDER BY createdAt DESC")
    suspend fun getAllCards(): List<WordCardEntity>

    @Query("SELECT * FROM word_cards WHERE id = :id")
    suspend fun getCardById(id: Long): WordCardEntity?

    @Query("SELECT * FROM word_cards WHERE id = :id")
    fun getCardByIdFlow(id: Long): Flow<WordCardEntity?>

    @Query("SELECT * FROM word_cards WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchCardsFlow(query: String): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteCardsFlow(): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards WHERE nextReviewAt <= :currentTime ORDER BY nextReviewAt ASC")
    fun getCardsDueForReviewFlow(currentTime: Long): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards WHERE reminderEnabled = 1")
    suspend fun getCardsWithActiveReminders(): List<WordCardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: WordCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<WordCardEntity>): List<Long>

    @Update
    suspend fun updateCard(card: WordCardEntity)

    @Delete
    suspend fun deleteCard(card: WordCardEntity)

    @Query("DELETE FROM word_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)

    @Query("DELETE FROM word_cards")
    suspend fun deleteAllCards()

    @Query("UPDATE word_cards SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, updatedAt: Long)

    @Query("UPDATE word_cards SET reviewLevel = :level, lastReviewedAt = :lastReviewedAt, nextReviewAt = :nextReviewAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReviewProgress(
        id: Long,
        level: Int,
        lastReviewedAt: Long,
        nextReviewAt: Long,
        updatedAt: Long
    )

    @Query("UPDATE word_cards SET nextReviewAt = :nextReviewAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNextReviewTime(
        id: Long,
        nextReviewAt: Long,
        updatedAt: Long
    )

    @Query("UPDATE word_cards SET reminderEnabled = :enabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, enabled: Boolean, updatedAt: Long)
}
