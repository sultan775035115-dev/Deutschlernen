package com.deutscherinnerungen.app.settings

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val allRemindersEnabled: Boolean = true
)
