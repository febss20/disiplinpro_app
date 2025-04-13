package com.example.disiplinpro

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.disiplinpro.viewmodel.auth.AuthViewModel
import com.example.disiplinpro.ui.auth.LoginScreen
import com.example.disiplinpro.ui.auth.OnboardingScreen
import com.example.disiplinpro.ui.auth.RegisterScreen
import com.example.disiplinpro.ui.home.HomeScreen
import com.example.disiplinpro.ui.auth.ForgotPasswordScreen
import com.example.disiplinpro.ui.auth.EmailVerificationScreen
import com.example.disiplinpro.ui.calender.CalendarScreen
import com.example.disiplinpro.ui.notification.NotificationScreen
import com.example.disiplinpro.ui.schedule.AddScheduleScreen
import com.example.disiplinpro.ui.schedule.AllSchedulesScreen
import com.example.disiplinpro.ui.schedule.EditScheduleScreen
import com.example.disiplinpro.ui.task.AddTaskScreen
import com.example.disiplinpro.ui.task.AllTasksScreen
import com.example.disiplinpro.ui.task.EditTaskScreen
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.disiplinpro.ui.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Izin notifikasi diberikan")
        } else {
            Toast.makeText(this, "Izin notifikasi diperlukan untuk pengingat!", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        requestBatteryOptimizationExemption()
        setContent {
            val navController = rememberNavController()
            val authViewModel = AuthViewModel()
            val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                "home"
            } else {
                "onboarding"
            }
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
                composable("akun") { ProfileScreen(navController) }

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
}