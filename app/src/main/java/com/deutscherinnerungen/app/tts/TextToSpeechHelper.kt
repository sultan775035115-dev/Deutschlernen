package com.deutscherinnerungen.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * مدير النطق الصوتي الآلي المدمج (Text-to-Speech)
 * يدعم نطق الكلمات الألمانية تلقائياً مع دعم اللغات الأخرى
 */
class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isReady = false

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // محاولة ضبط اللغة الافتراضية إلى الألمانية أولاً
            val result = textToSpeech?.setLanguage(Locale.GERMAN)
            isReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
            if (!isReady) {
                // إذا لم تكن الألمانية متوفرة، نستخدم الإنجليزية أو لغة الجهاز الافتراضية
                textToSpeech?.setLanguage(Locale.ENGLISH)
                isReady = true
            }
        } else {
            isReady = false
        }
    }

    /**
     * نطق النص فوراً
     */
    fun speak(text: String, locale: Locale = Locale.GERMAN) {
        if (text.isBlank()) return
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        }

        try {
            textToSpeech?.language = locale
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "WordAnchor_TTS_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isReady = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
