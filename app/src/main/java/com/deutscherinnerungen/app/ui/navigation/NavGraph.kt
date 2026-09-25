package com.deutscherinnerungen.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.deutscherinnerungen.app.WordAnchorApp
import com.deutscherinnerungen.app.backup.BackupManager
import com.deutscherinnerungen.app.ui.screens.AddEditCardScreen
import com.deutscherinnerungen.app.ui.screens.CardDetailScreen
import com.deutscherinnerungen.app.ui.screens.HomeScreen
import com.deutscherinnerungen.app.ui.screens.SettingsScreen
import com.deutscherinnerungen.app.ui.viewmodel.AddEditCardViewModel
import com.deutscherinnerungen.app.ui.viewmodel.CardDetailViewModel
import com.deutscherinnerungen.app.ui.viewmodel.CardsViewModel
import com.deutscherinnerungen.app.ui.viewmodel.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    app: WordAnchorApp,
    startDestination: String = Screen.Home.route
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // الشاشة الرئيسية
        composable(Screen.Home.route) {
            val cardsViewModel: CardsViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return CardsViewModel(
                            repository = app.repository,
                            mediaManager = app.mediaManager,
                            reminderScheduler = app.reminderScheduler
                        ) as T
                    }
                }
            )

            HomeScreen(
                viewModel = cardsViewModel,
                onNavigateToAddCard = {
                    navController.navigate(Screen.AddEditCard.createRoute())
                },
                onNavigateToCardDetail = { cardId ->
                    navController.navigate(Screen.CardDetail.createRoute(cardId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // شاشة تفاصيل ومراجعة البطاقة
        composable(
            route = Screen.CardDetail.route,
            arguments = listOf(
                navArgument("cardId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            val detailViewModel: CardDetailViewModel = viewModel(
                key = "card_$cardId",
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return CardDetailViewModel(
                            cardId = cardId,
                            repository = app.repository,
                            mediaManager = app.mediaManager,
                            reminderScheduler = app.reminderScheduler
                        ) as T
                    }
                }
            )

            CardDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { editCardId ->
                    navController.navigate(Screen.AddEditCard.createRoute(editCardId))
                }
            )
        }

        // شاشة إضافة أو تعديل بطاقة
        composable(
            route = Screen.AddEditCard.route,
            arguments = listOf(
                navArgument("cardId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val cardIdStr = backStackEntry.arguments?.getString("cardId")
            val cardId = cardIdStr?.toLongOrNull()

            val addEditViewModel: AddEditCardViewModel = viewModel(
                key = "add_edit_${cardId ?: "new"}",
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return AddEditCardViewModel(
                            cardId = cardId,
                            repository = app.repository,
                            mediaManager = app.mediaManager,
                            reminderScheduler = app.reminderScheduler
                        ) as T
                    }
                }
            )

            AddEditCardScreen(
                viewModel = addEditViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // شاشة الإعدادات
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return SettingsViewModel(
                            settingsRepository = app.settingsRepository,
                            cardRepository = app.repository,
                            backupManager = BackupManager(context),
                            reminderScheduler = app.reminderScheduler,
                            notificationHelper = app.notificationHelper,
                            mediaManager = app.mediaManager
                        ) as T
                    }
                }
            )

            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
