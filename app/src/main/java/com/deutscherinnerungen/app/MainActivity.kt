package com.deutscherinnerungen.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.deutscherinnerungen.app.notifications.NotificationHelper
import com.deutscherinnerungen.app.settings.AppSettings
import com.deutscherinnerungen.app.ui.navigation.NavGraph
import com.deutscherinnerungen.app.ui.navigation.Screen
import com.deutscherinnerungen.app.ui.theme.WordAnchorTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // التعامل مع منح أو رفض إذن الإشعارات في أندرويد 13 فما فوق
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // طلب صلاحية الإشعارات لأندرويد 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as WordAnchorApp

        setContent {
            val appSettings by app.settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
            val navController = rememberNavController()

            WordAnchorTheme(themeMode = appSettings.themeMode) {
                // التحقق مما إذا كان التطبيق قد تم فتحه عبر الضغط على إشعار
                val targetCardId = intent?.getLongExtra(NotificationHelper.EXTRA_CARD_ID, -1L) ?: -1L
                val startDestination = if (targetCardId > 0) {
                    Screen.CardDetail.createRoute(targetCardId)
                } else {
                    Screen.Home.route
                }

                NavGraph(
                    navController = navController,
                    app = app,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
