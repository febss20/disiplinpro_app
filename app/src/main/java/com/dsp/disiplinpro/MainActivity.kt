package com.dsp.disiplinpro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dsp.disiplinpro.navigation.DisiplinProNavHost
import com.dsp.disiplinpro.navigation.NavigationHandler
import com.dsp.disiplinpro.permissions.PermissionManager
import com.dsp.disiplinpro.data.security.SecurityManager
import com.dsp.disiplinpro.ui.components.BottomNavigationBar
import com.dsp.disiplinpro.ui.components.getBackgroundColorForCurrentTheme
import com.dsp.disiplinpro.ui.theme.DisiplinproTheme
import com.dsp.disiplinpro.viewmodel.auth.AuthViewModel
import com.dsp.disiplinpro.viewmodel.theme.ThemeViewModel
import com.dsp.disiplinpro.worker.NotificationRestorer
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.dsp.disiplinpro.util.NavigationBarUtils
import androidx.compose.ui.platform.LocalContext

class MainActivity : FragmentActivity() {
    private val themeViewModel = ThemeViewModel()
    private lateinit var securityManager: SecurityManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var navigationHandler: NavigationHandler
    private lateinit var notificationRestorer: NotificationRestorer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeManagers()

        configureWindow()

        permissionManager.requestAllPermissions()

        navigationHandler.processNotificationIntent(intent)

        themeViewModel.initialize(this)

        setupUserInterface()

        CoroutineScope(Dispatchers.IO).launch {
            notificationRestorer.checkAndRestoreScheduledNotifications()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navigationHandler.processNotificationIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        securityManager.checkSessionValidity(this)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        securityManager.extendSession()
    }

    private fun initializeManagers() {
        securityManager = SecurityManager(this)
        permissionManager = PermissionManager(this)
        navigationHandler = NavigationHandler()
        notificationRestorer = NotificationRestorer(this)
    }

    private fun configureWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
    }

    private fun setupUserInterface() {
        setContent {
            val navController = rememberNavController()
            val authViewModel = AuthViewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val systemUiController = rememberSystemUiController()
            val backgroundColor = getBackgroundColorForCurrentTheme()
            val context = LocalContext.current
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val isGestureNavigation = NavigationBarUtils.isGestureNavigation(context)
            val bottomPadding = if (isGestureNavigation) 25.dp else 55.dp

            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = !isDarkMode
            )
            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = !isDarkMode
            )
            systemUiController.systemBarsDarkContentEnabled = !isDarkMode
            systemUiController.isStatusBarVisible = true
            systemUiController.isNavigationBarVisible = true

            authViewModel.initialize(this)

            DisiplinproTheme(darkTheme = isDarkMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                ) {
                    navigationHandler.HandleNotificationNavigation(navController)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (shouldShowBottomBar(currentRoute)) 115.dp else 0.dp)
                    ) {
                        DisiplinProNavHost(
                            navController = navController,
                            themeViewModel = themeViewModel,
                            backgroundColor = backgroundColor,
                            authViewModel = authViewModel
                        )
                    }

                    if (shouldShowBottomBar(currentRoute)) {
                        BottomNavigationBar(
                            navController = navController,
                            currentRoute = currentRoute,
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.BottomCenter)
                                .padding(bottom = bottomPadding)
                        )
                    }
                }
            }
        }
    }

    private fun shouldShowBottomBar(currentRoute: String?): Boolean {
        return currentRoute == "home" ||
                currentRoute == "kalender" ||
                currentRoute == "notifikasi" ||
                currentRoute == "akun" ||
                currentRoute == "list_jadwal" ||
                currentRoute == "list_tugas"

    }
}