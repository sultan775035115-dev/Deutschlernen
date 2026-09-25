package com.deutscherinnerungen.app.srs

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * محرك التكرار المتباعد (Spaced Repetition System - SRS)
 * مفصول تماماً عن طبقة واجهة المستخدم لسهولة استبداله أو تطويره بخوارزميات أخرى مستقبلاً (مثل SuperMemo-2 أو FSRS)
 */
object SpacedRepetitionEngine {

    /**
     * الفترات الافتراضية بالأيام لكل مستوى مراجعة
     * Level 0: جديدة
     * Level 1: يوم واحد
     * Level 2: 3 أيام
     * Level 3: 7 أيام
     * Level 4: 14 يوماً
     * Level 5: 30 يوماً
     * Level 6: 60 يوماً
     */
    val REVIEW_INTERVALS_DAYS = listOf(
        0,  // المستوى 0: بطاقة جديدة لم تتم مراجعتها بعد
        1,  // المستوى 1: بعد يوم واحد
        3,  // المستوى 2: بعد 3 أيام
        7,  // المستوى 3: بعد 7 أيام
        14, // المستوى 4: بعد 14 يوماً
        30, // المستوى 5: بعد 30 يوماً
        60  // المستوى 6: بعد 60 يوماً (تثبيت تام)
    )

    const val MAX_LEVEL = 6

    data class ReviewStepResult(
        val nextLevel: Int,
        val nextReviewAt: Long,
        val isMastered: Boolean
    )

    /**
     * حساب المستوى وتاريخ المراجعة التالي عند الضغط على "راجعتها"
     */
    fun calculateNextReview(currentLevel: Int, baseTime: Long = System.currentTimeMillis()): ReviewStepResult {
        val nextLevel = (currentLevel + 1).coerceAtMost(MAX_LEVEL)
        val daysToAdd = REVIEW_INTERVALS_DAYS[nextLevel]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTime
            add(Calendar.DAY_OF_YEAR, daysToAdd)
        }

        return ReviewStepResult(
            nextLevel = nextLevel,
            nextReviewAt = calendar.timeInMillis,
            isMastered = nextLevel >= MAX_LEVEL
        )
    }

    /**
     * خيارات "أعد التذكير لاحقاً" (Snooze)
     */
    enum class SnoozeOption {
        AFTER_10_MINUTES,
        AFTER_1_HOUR,
        TOMORROW,
        CUSTOM
    }

    /**
     * حساب وقت التذكير اللاحق
     */
    fun calculateSnoozeTime(
        option: SnoozeOption,
        customTimeMillis: Long? = null,
        baseTime: Long = System.currentTimeMillis()
    ): Long {
        return when (option) {
            SnoozeOption.AFTER_10_MINUTES -> baseTime + TimeUnit.MINUTES.toMillis(10)
            SnoozeOption.AFTER_1_HOUR -> baseTime + TimeUnit.HOURS.toMillis(1)
            SnoozeOption.TOMORROW -> {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = baseTime
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                calendar.timeInMillis
            }
            SnoozeOption.CUSTOM -> customTimeMillis ?: (baseTime + TimeUnit.HOURS.toMillis(2))
        }
    }

    /**
     * الحصول على التسمية العربية لمستوى المراجعة
     */
    fun getLevelTitleArabic(level: Int): String {
        return when (level) {
            0 -> "جديدة"
            1 -> "المستوى 1 (يوم)"
            2 -> "المستوى 2 (3 أيام)"
            3 -> "المستوى 3 (7 أيام)"
            4 -> "المستوى 4 (14 يوماً)"
            5 -> "المستوى 5 (30 يوماً)"
            6 -> "المستوى 6 (مثبتة - 60 يوماً)"
            else -> "المستوى $level"
        }
    }
}
