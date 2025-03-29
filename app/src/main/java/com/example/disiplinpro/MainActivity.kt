package com.example.disiplinpro

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
import com.example.disiplinpro.ui.schedule.AddScheduleScreen
import com.example.disiplinpro.ui.schedule.AllSchedulesScreen
import com.example.disiplinpro.ui.task.AddTaskScreen
import com.example.disiplinpro.ui.task.AllTasksScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val authViewModel = AuthViewModel()
            NavHost(navController = navController, startDestination = "onboarding") {
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
                composable("add_tugas") { AddTaskScreen(navController) }
                composable("list_jadwal") { AllSchedulesScreen(navController) }
                composable("list_tugas") { AllTasksScreen(navController) }

            }
        }
    }
}