package com.deutscherinnerungen.app.backup

import android.content.Context
import android.net.Uri
import com.deutscherinnerungen.app.data.database.WordAnchorDatabase
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * بيانات حزمة التصدير والاستيراد
 */
data class BackupData(
    val appName: String = "WordAnchor",
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val cardsCount: Int,
    val cards: List<WordCardEntity>
)

/**
 * مدير النسخ الاحتياطي وتصدير واستيراد البيانات بتنسيق JSON
 */
class BackupManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * تصدير جميع البطاقات إلى ملف JSON عبر URI
     */
    suspend fun exportToJson(destinationUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val db = WordAnchorDatabase.getInstance(context)
            val cards = db.wordCardDao().getAllCards()

            val backupData = BackupData(
                cardsCount = cards.size,
                cards = cards
            )

            val jsonString = gson.toJson(backupData)

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(Exception("تعذر فتح ملف الوجهة للكتابة"))

            Result.success(cards.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * استيراد البطاقات من ملف JSON عبر URI
     */
    suspend fun importFromJson(sourceUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonContent = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("تعذر قراءة ملف النسخة الاحتياطية"))

            val backupType = object : TypeToken<BackupData>() {}.type
            val backupData: BackupData = gson.fromJson(jsonContent, backupType)

            if (backupData.cards.isEmpty()) {
                return@withContext Result.success(0)
            }

            val db = WordAnchorDatabase.getInstance(context)

            // إعادة تعيين الـ id إلى 0 لإدخالها كبطاقات جديدة دون تصادم مع البطاقات الحالية
            val normalizedCards = backupData.cards.map { card ->
                card.copy(id = 0L)
            }

            db.wordCardDao().insertCards(normalizedCards)

            Result.success(normalizedCards.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
