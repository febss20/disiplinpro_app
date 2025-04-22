package com.example.disiplinpro

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
import com.example.disiplinpro.ui.auth.EmailVerificationScreen
import com.example.disiplinpro.ui.auth.ForgotPasswordScreen
import com.example.disiplinpro.ui.auth.LoginScreen
import com.example.disiplinpro.ui.auth.OnboardingScreen
import com.example.disiplinpro.ui.auth.RegisterScreen
import com.example.disiplinpro.ui.calender.CalendarScreen
import com.example.disiplinpro.ui.home.HomeScreen
import com.example.disiplinpro.ui.notification.NotificationHistoryScreen
import com.example.disiplinpro.ui.notification.NotificationScreen
import com.example.disiplinpro.ui.profile.ProfileScreen
import com.example.disiplinpro.ui.profile.ProfileEditScreen
import com.example.disiplinpro.ui.profile.SecurityPrivacyScreen
import com.example.disiplinpro.ui.profile.FAQScreen
import com.example.disiplinpro.ui.schedule.AddScheduleScreen
import com.example.disiplinpro.ui.schedule.AllSchedulesScreen
import com.example.disiplinpro.ui.schedule.EditScheduleScreen
import com.example.disiplinpro.ui.task.AddTaskScreen
import com.example.disiplinpro.ui.task.AllTasksScreen
import com.example.disiplinpro.ui.task.EditTaskScreen
import com.example.disiplinpro.viewmodel.auth.AuthViewModel
import com.example.disiplinpro.worker.NotificationWorker
import com.google.firebase.auth.FirebaseAuth

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

    private var notificationType: String? = null
    private var notificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        requestBatteryOptimizationExemption()
        processNotificationIntent(intent)
        setupNavigation()
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

            LaunchedEffect(Unit) {
                authViewModel.initialize(this@MainActivity)
            }

            val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                "home"
            } else {
                "onboarding"
            }

            HandleNotificationNavigation(navController)

            NavHost(navController = navController, startDestination = startDestination) {
                composable("onboarding") { OnboardingScreen(navController) }
                composable("login") { LoginScreen(navController) }
                composable("register") { RegisterScreen(navController) }
                composable("home") { HomeScreen(navController) }
                composable("forgot_password") { ForgotPasswordScreen(navController, authViewModel) }
                composable("email_verification/{email}") { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    EmailVerificationScreen(navController, email, authViewModel)
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
                composable("notification_history") { NotificationHistoryScreen(navController) }
                composable("akun") { ProfileScreen(navController) }
                composable("edit_akun") { ProfileEditScreen(navController) }
                composable("keamanan_privasi") { SecurityPrivacyScreen(navController) }
                composable("faq") { FAQScreen(navController) }
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
}