package com.dsp.disiplinpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dsp.disiplinpro.ui.auth.EmailVerificationScreen
import com.dsp.disiplinpro.ui.auth.ForgotPasswordScreen
import com.dsp.disiplinpro.ui.auth.LoginScreen
import com.dsp.disiplinpro.ui.auth.OnboardingScreen
import com.dsp.disiplinpro.ui.auth.RegisterScreen
import com.dsp.disiplinpro.ui.calender.CalendarScreen
import com.dsp.disiplinpro.ui.home.HomeScreen
import com.dsp.disiplinpro.ui.notification.NotificationListScreen
import com.dsp.disiplinpro.ui.notification.NotificationScreen
import com.dsp.disiplinpro.ui.profile.ProfileScreen
import com.dsp.disiplinpro.ui.profile.ProfileEditScreen
import com.dsp.disiplinpro.ui.profile.SecurityPrivacyScreen
import com.dsp.disiplinpro.ui.profile.FAQScreen
import com.dsp.disiplinpro.ui.schedule.AddScheduleScreen
import com.dsp.disiplinpro.ui.schedule.AllSchedulesScreen
import com.dsp.disiplinpro.ui.schedule.EditScheduleScreen
import com.dsp.disiplinpro.ui.task.AddTaskScreen
import com.dsp.disiplinpro.ui.task.AllTasksScreen
import com.dsp.disiplinpro.ui.task.EditTaskScreen
import com.dsp.disiplinpro.viewmodel.auth.AuthViewModel
import com.dsp.disiplinpro.worker.NotificationWorker
import com.google.firebase.auth.FirebaseAuth
import com.dsp.disiplinpro.data.repository.FirestoreRepository
import com.dsp.disiplinpro.viewmodel.notification.NotificationViewModel
import com.dsp.disiplinpro.viewmodel.task.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import com.dsp.disiplinpro.worker.NotificationHealthCheckWorker
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsp.disiplinpro.ui.theme.DisiplinproTheme
import com.dsp.disiplinpro.viewmodel.theme.ThemeViewModel
import com.dsp.disiplinpro.ui.auth.TwoFactorSetupScreen
import com.dsp.disiplinpro.ui.auth.TwoFactorVerificationScreen
import com.dsp.disiplinpro.viewmodel.auth.TwoFactorAuthViewModel
import com.dsp.disiplinpro.data.security.AppSecurityPolicy
import com.google.firebase.firestore.BuildConfig
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.compose.runtime.Composable
import com.dsp.disiplinpro.ui.theme.Spring
import com.dsp.disiplinpro.ui.components.getBackgroundColorForCurrentTheme

class MainActivity : FragmentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Izin notifikasi diberikan")
        } else {
            Toast.makeText(this, "Izin notifikasi diperlukan untuk pengingat!", Toast.LENGTH_LONG).show()
        }
    }

    private val requestAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("MainActivity", "Hasil permintaan izin alarm: ${it.resultCode}")
    }

    private var notificationType: String? = null
    private var notificationId: String? = null
    private val themeViewModel = ThemeViewModel()
    private lateinit var securityPolicy: AppSecurityPolicy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        initializeSecurityPolicy()

        requestNotificationPermission()
        requestBatteryOptimizationExemption()
        requestExactAlarmPermission()
        processNotificationIntent(intent)
        themeViewModel.initialize(this)
        setupNavigation()

        CoroutineScope(Dispatchers.IO).launch {
            checkAndRestoreScheduledNotifications()
        }

        demonstrateSecureHttpClient()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processNotificationIntent(intent)
        setIntent(intent)
    }

    private fun processNotificationIntent(intent: Intent) {
        notificationType = intent.getStringExtra(NotificationWorker.EXTRA_NOTIFICATION_TYPE)
        notificationId = intent.getStringExtra(NotificationWorker.EXTRA_ID)

        if (notificationType != null && notificationId != null) {
            Log.d("MainActivity", "Received notification: type=$notificationType, id=$notificationId")
        }
    }

    private fun setupNavigation() {
        setContent {
            val navController = rememberNavController()
            val authViewModel = AuthViewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val systemUiController = rememberSystemUiController()
            val backgroundColor = getBackgroundColorForCurrentTheme()

            LaunchedEffect(isDarkMode) {
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
            }

            LaunchedEffect(Unit) {
                authViewModel.initialize(this@MainActivity)
            }

            val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                "home"
            } else {
                "onboarding"
            }

            DisiplinproTheme(darkTheme = isDarkMode) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)){

                    HandleNotificationNavigation(navController)

                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.background(backgroundColor)
                    ) {
                        composable(
                            route = "onboarding",
                            enterTransition = {
                                fadeIn(animationSpec = tween(700))
                            },
                            exitTransition = {
                                fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeOut(animationSpec = tween(300))
                            }
                        ) { OnboardingScreen(navController) }
                        composable(
                            route = "login",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeIn(
                                    initialAlpha = 0.3f,
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeOut(animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeIn(
                                    initialAlpha = 0.3f,
                                    animationSpec = tween(300)
                                )
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeOut(animationSpec = tween(300))
                            }
                        ) {
                            Box(modifier = Modifier.background(backgroundColor)) {
                                LoginScreen(navController)
                            }
                        }
                        composable(
                            route = "register",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeIn(
                                    initialAlpha = 0.3f,
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeOut(animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeIn(
                                    initialAlpha = 0.3f,
                                    animationSpec = tween(300)
                                )
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeOut(animationSpec = tween(300))
                            }
                        ) {
                            Box(modifier = Modifier.background(backgroundColor)) {
                                RegisterScreen(navController)
                            }
                        }
                        composable(
                            route = "home",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) + fadeIn(
                                    initialAlpha = 0.3f,
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeOut(animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = EaseInOut)
                                ) + fadeIn(
                                    initialAlpha = 0.3f,
                                    animationSpec = tween(300)
                                )
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) + fadeOut(animationSpec = tween(300))
                            }
                        ) {
                            Box(modifier = Modifier.background(backgroundColor)) {
                                HomeScreen(navController)
                            }
                        }

                        fun NavGraphBuilder.composableWithAnimations(
                            route: String,
                            content: @Composable (NavBackStackEntry) -> Unit
                        ) {
                            composable(
                                route = route,
                                enterTransition = {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(300, easing = EaseInOut)
                                    ) + fadeIn(
                                        initialAlpha = 0.3f,
                                        animationSpec = tween(300)
                                    )
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(300, easing = EaseInOut)
                                    ) + fadeOut(animationSpec = tween(300))
                                },
                                popEnterTransition = {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(300, easing = EaseInOut)
                                    ) + fadeIn(
                                        initialAlpha = 0.3f,
                                        animationSpec = tween(300)
                                    )
                                },
                                popExitTransition = {
                                    slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(300, easing = EaseInOut)
                                    ) + fadeOut(animationSpec = tween(300))
                                }
                            ) {
                                Box(modifier = Modifier.background(backgroundColor)) {
                                    content(it)
                                }
                            }
                        }

                        composableWithAnimations("forgot_password") {
                            ForgotPasswordScreen(navController, authViewModel)
                        }

                        composableWithAnimations("email_verification/{email}") { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            EmailVerificationScreen(navController, email, authViewModel)
                        }

                        composableWithAnimations("two_factor_setup") {
                            TwoFactorSetupScreen(navController)
                        }

                        composableWithAnimations("two_factor_verification/{email}") { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            val twoFactorViewModel = viewModel<TwoFactorAuthViewModel>()
                            TwoFactorVerificationScreen(
                                navController,
                                authViewModel,
                                email,
                                twoFactorViewModel
                            )
                        }

                        composableWithAnimations("add_jadwal") {
                            AddScheduleScreen(navController)
                        }

                        composableWithAnimations("edit_jadwal/{scheduleId}") { backStackEntry ->
                            val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: ""
                            EditScheduleScreen(navController, scheduleId)
                        }

                        composableWithAnimations("list_jadwal") {
                            AllSchedulesScreen(navController)
                        }

                        composableWithAnimations("add_tugas") {
                            AddTaskScreen(navController)
                        }

                        composableWithAnimations("edit_tugas/{taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                            EditTaskScreen(navController, taskId)
                        }

                        composableWithAnimations("list_tugas") {
                            AllTasksScreen(navController)
                        }

                        composableWithAnimations("kalender") {
                            CalendarScreen(navController)
                        }

                        composableWithAnimations("notifikasi") {
                            NotificationScreen(navController)
                        }

                        composableWithAnimations("akun") {
                            ProfileScreen(
                                navController,
                                themeViewModel = themeViewModel
                            )
                        }

                        composableWithAnimations("edit_akun") {
                            ProfileEditScreen(
                                navController,
                                themeViewModel = themeViewModel
                            )
                        }

                        composableWithAnimations("keamanan_privasi") {
                            SecurityPrivacyScreen(
                                navController,
                                themeViewModel = themeViewModel
                            )
                        }

                        composableWithAnimations("faq") {
                            FAQScreen(
                                navController,
                                themeViewModel = themeViewModel
                            )
                        }

                        composableWithAnimations("notification_list") {
                            NotificationListScreen(navController)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HandleNotificationNavigation(navController: NavHostController) {
        val type = remember { notificationType }
        val id = remember { notificationId }

        LaunchedEffect(type, id) {
            if (type != null && id != null) {
                when (type) {
                    NotificationWorker.TYPE_TASK -> {
                        Log.d("MainActivity", "Navigating to all task screen")
                        navController.navigate("list_tugas") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                    NotificationWorker.TYPE_SCHEDULE -> {
                        Log.d("MainActivity", "Navigating to schedule all screen")
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

    private suspend fun checkAndRestoreScheduledNotifications() {
        try {
            val activeWorkInfos = WorkManager.getInstance(this).getWorkInfosByTag("notification_tag").get()

            if (activeWorkInfos.isEmpty()) {
                Log.d("MainActivity", "Tidak ada pekerjaan terjadwal aktif, memulihkan notifikasi...")

                val notificationViewModel = NotificationViewModel()
                val taskViewModel = TaskViewModel()

                val repository = FirestoreRepository()
                val tasks = repository.getTasks()
                val schedules = repository.getSchedules()

                tasks.filter { !it.isCompleted }.forEach { task ->
                    Log.d("MainActivity", "Memulihkan notifikasi untuk tugas: ${task.judulTugas}")
                    notificationViewModel.scheduleNotification(this, task)
                }

                schedules.forEach { schedule ->
                    Log.d("MainActivity", "Memulihkan notifikasi untuk jadwal: ${schedule.matkul}")
                    notificationViewModel.scheduleNotification(this, schedule)
                }

                scheduleNotificationHealthCheck()

                Log.d("MainActivity", "Pemulihan notifikasi selesai")
            } else {
                Log.d("MainActivity", "Ditemukan ${activeWorkInfos.size} pekerjaan terjadwal aktif, pemulihan tidak diperlukan")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saat memeriksa/memulihkan notifikasi: ${e.message}")
        }
    }

    private fun scheduleNotificationHealthCheck() {
        val healthCheckRequest = OneTimeWorkRequestBuilder<NotificationHealthCheckWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .addTag("health_check_tag")
            .build()

        WorkManager.getInstance(this)
            .enqueue(healthCheckRequest)

        Log.d("MainActivity", "Pemeriksaan kesehatan notifikasi dijadwalkan untuk 24 jam berikutnya")
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:$packageName")
                requestAlarmPermissionLauncher.launch(intent)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Izin notifikasi sudah diberikan")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        this,
                        "DisiplinPro memerlukan izin notifikasi untuk mengingatkan Anda tentang tugas dan jadwal",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun initializeSecurityPolicy() {
        securityPolicy = AppSecurityPolicy(this)
        securityPolicy.initialize()

        checkDeviceSecurity()
    }

    private fun checkDeviceSecurity() {
        if (securityPolicy.isDeviceRooted()) {
            Log.w("MainActivity", "Perangkat terdeteksi dalam kondisi root!")
        }

        if (securityPolicy.isRunningOnEmulator() && !BuildConfig.DEBUG) {
            Log.w("MainActivity", "Aplikasi berjalan di emulator dalam mode rilis!")
        }

        if (securityPolicy.isBeingDebugged() && !BuildConfig.DEBUG) {
            Log.w("MainActivity", "Aplikasi sedang di-debug dalam mode rilis!")
        }
    }

    override fun onResume() {
        super.onResume()

        if (FirebaseAuth.getInstance().currentUser != null && !securityPolicy.isSessionValid()) {
            Log.d("MainActivity", "Sesi telah kedaluwarsa (durasi sesi: 7 hari), mengarahkan ke login")
            Toast.makeText(
                this,
                "Sesi login Anda telah berakhir. Silakan login kembali.",
                Toast.LENGTH_LONG
            ).show()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        securityPolicy.extendSession()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        securityPolicy.extendSession()
    }

    private fun demonstrateSecureHttpClient() {
        try {
            val client = DisiplinProApplication.secureHttpClient
            Log.d("MainActivity", "Secure HTTP Client ready: ${client != null}")

        } catch (e: Exception) {
            Log.e("MainActivity", "Error accessing secure HTTP client: ${e.message}")
        }
    }
}