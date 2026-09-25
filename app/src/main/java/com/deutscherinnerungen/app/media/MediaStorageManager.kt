package com.deutscherinnerungen.app.media

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * مدير تخزين الوسائط محلياً في ذاكرة التطبيق الداخلية (filesDir/media/...)
 * يضمن بقاء الملفات حتى لو حذف المستخدم الأصل من المعرض، ويمنع ترك ملفات يتيمة
 */
class MediaStorageManager(private val context: Context) {

    enum class MediaType(val subDir: String) {
        IMAGE("images"),
        AUDIO("audio"),
        VIDEO("videos")
    }

    private fun getMediaDirectory(type: MediaType): File {
        val dir = File(context.filesDir, "media/${type.subDir}")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * نسخ ملف من URI خارجي (PhotoPicker / SAF) إلى مساحة التخزين الداخلية للتطبيق
     */
    suspend fun copyUriToInternalStorage(uri: Uri, type: MediaType): String? = withContext(Dispatchers.IO) {
        try {
            val extension = getExtensionFromUri(uri, type)
            val fileName = "${type.name.lowercase()}_${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
            val targetDir = getMediaDirectory(type)
            val targetFile = File(targetDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext null

            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * حذف ملف وسائط من التخزين الداخلي لمنع تراكم الملفات اليتيمة
     */
    suspend fun deleteInternalFile(filePath: String?): Boolean = withContext(Dispatchers.IO) {
        if (filePath.isNullOrBlank()) return@withContext false
        try {
            val file = File(filePath)
            if (file.exists() && file.canonicalPath.startsWith(context.filesDir.canonicalPath)) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * حذف جميع الوسائط المرتبطة ببطاقة معينة
     */
    suspend fun deleteCardMedia(imagePath: String?, audioPath: String?, videoPath: String?) {
        deleteInternalFile(imagePath)
        deleteInternalFile(audioPath)
        deleteInternalFile(videoPath)
    }

    /**
     * التحقق من وجود الملف
     */
    fun fileExists(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }

    private fun getExtensionFromUri(uri: Uri, type: MediaType): String {
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (!ext.isNullOrBlank()) return ext
        }
        return when (type) {
            MediaType.IMAGE -> "jpg"
            MediaType.AUDIO -> "m4a"
            MediaType.VIDEO -> "mp4"
        }
    }
}
