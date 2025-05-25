package com.dsp.disiplinpro.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.dsp.disiplinpro.worker.NotificationWorker

class NavigationHandler {
    private var notificationType: String? = null
    private var notificationId: String? = null

    fun processNotificationIntent(intent: Intent) {
        notificationType = intent.getStringExtra(NotificationWorker.EXTRA_NOTIFICATION_TYPE)
        notificationId = intent.getStringExtra(NotificationWorker.EXTRA_ID)

        if (notificationType != null && notificationId != null) {
            Log.d("NavigationHandler", "Received notification: type=$notificationType, id=$notificationId")
        }
    }
    @Composable
    fun HandleNotificationNavigation(navController: NavHostController) {
        val type = remember { notificationType }
        val id = remember { notificationId }

        LaunchedEffect(type, id) {
            if (type != null && id != null) {
                when (type) {
                    NotificationWorker.TYPE_TASK -> {
                        Log.d("NavigationHandler", "Navigating to all task screen")
                        navController.navigate("list_tugas") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                    NotificationWorker.TYPE_SCHEDULE -> {
                        Log.d("NavigationHandler", "Navigating to schedule all screen")
                        navController.navigate("list_jadwal") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }

                notificationType = null
                notificationId = null
            }
        }
    }
}