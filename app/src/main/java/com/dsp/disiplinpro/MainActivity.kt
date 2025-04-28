package com.dsp.disiplinpro

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsp.disiplinpro.ui.theme.DisiplinproTheme
import com.dsp.disiplinpro.viewmodel.theme.ThemeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.dsp.disiplinpro.ui.auth.TwoFactorSetupScreen
import com.dsp.disiplinpro.ui.auth.TwoFactorVerificationScreen
import com.dsp.disiplinpro.viewmodel.auth.TwoFactorAuthViewModel

class MainActivity : ComponentActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupFirestoreOfflineCache()

        requestNotificationPermission()
        requestBatteryOptimizationExemption()
        requestExactAlarmPermission()
        processNotificationIntent(intent)
        themeViewModel.initialize(this)
        setupNavigation()

        CoroutineScope(Dispatchers.IO).launch {
            checkAndRestoreScheduledNotifications()
        }
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

            LaunchedEffect(Unit) {
                authViewModel.initialize(this@MainActivity)
            }

            val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                "home"
            } else {
                "onboarding"
            }

            DisiplinproTheme(darkTheme = isDarkMode) {
                HandleNotificationNavigation(navController)

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("onboarding") { OnboardingScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            navController,
                            authViewModel
                        )
                    }
                    composable("email_verification/{email}") { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        EmailVerificationScreen(navController, email, authViewModel)
                    }
                    composable("two_factor_setup") {
                        TwoFactorSetupScreen(navController)
                    }
                    composable("two_factor_verification/{email}") { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        val twoFactorViewModel = viewModel<TwoFactorAuthViewModel>()
                        TwoFactorVerificationScreen(navController, authViewModel, email, twoFactorViewModel)
                    }
                    composable("add_jadwal") { AddScheduleScreen(navController) }
                    composable("edit_jadwal/{scheduleId}") { backStackEntry ->
                        val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: ""
                        EditScheduleScreen(navController, scheduleId)
                    }
                    composable("list_jadwal") { AllSchedulesScreen(navController) }
                    composable("add_tugas") { AddTaskScreen(navController) }
                    composable("edit_tugas/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                        EditTaskScreen(navController, taskId)
                    }
                    composable("list_tugas") { AllTasksScreen(navController) }
                    composable("kalender") { CalendarScreen(navController) }
                    composable("notifikasi") { NotificationScreen(navController) }
                    composable("akun") { ProfileScreen(navController, themeViewModel = themeViewModel) }
                    composable("edit_akun") { ProfileEditScreen(navController, themeViewModel = themeViewModel) }
                    composable("keamanan_privasi") { SecurityPrivacyScreen(navController, themeViewModel = themeViewModel) }
                    composable("faq") { FAQScreen(navController, themeViewModel = themeViewModel) }
                    composable("notification_list") { NotificationListScreen(navController) }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
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
                        }
                    }
                    NotificationWorker.TYPE_SCHEDULE -> {
                        Log.d("MainActivity", "Navigating to schedule all screen")
                        navController.navigate("list_jadwal") {

                            popUpTo("home") { inclusive = false }
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
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
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
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun setupFirestoreOfflineCache() {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings

        if (FirebaseAuth.getInstance().currentUser != null) {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).collection("tasks")
                .limit(20)
                .get()

            db.collection("users").document(userId).collection("schedules")
                .limit(20)
                .get()
        }
    }
}