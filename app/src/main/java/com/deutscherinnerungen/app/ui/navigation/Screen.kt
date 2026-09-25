package com.deutscherinnerungen.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEditCard : Screen("add_edit?cardId={cardId}") {
        fun createRoute(cardId: Long? = null): String {
            return if (cardId != null) "add_edit?cardId=$cardId" else "add_edit"
        }
    }
    object CardDetail : Screen("card_detail/{cardId}") {
        fun createRoute(cardId: Long): String = "card_detail/$cardId"
    }
    object Settings : Screen("settings")
}
