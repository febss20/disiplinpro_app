package com.dsp.disiplinpro.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsp.disiplinpro.ui.auth.EmailVerificationScreen
import com.dsp.disiplinpro.ui.auth.ForgotPasswordScreen
import com.dsp.disiplinpro.ui.auth.LoginScreen
import com.dsp.disiplinpro.ui.auth.OnboardingScreen
import com.dsp.disiplinpro.ui.auth.RegisterScreen
import com.dsp.disiplinpro.ui.auth.TwoFactorSetupScreen
import com.dsp.disiplinpro.ui.auth.TwoFactorVerificationScreen
import com.dsp.disiplinpro.ui.calender.CalendarScreen
import com.dsp.disiplinpro.ui.chat.ChatbotScreen
import com.dsp.disiplinpro.ui.home.HomeScreen
import com.dsp.disiplinpro.ui.notification.NotificationListScreen
import com.dsp.disiplinpro.ui.notification.NotificationScreen
import com.dsp.disiplinpro.ui.profile.FAQScreen
import com.dsp.disiplinpro.ui.profile.ProfileEditScreen
import com.dsp.disiplinpro.ui.profile.ProfileScreen
import com.dsp.disiplinpro.ui.profile.SecurityPrivacyScreen
import com.dsp.disiplinpro.ui.schedule.AddScheduleScreen
import com.dsp.disiplinpro.ui.schedule.AllSchedulesScreen
import com.dsp.disiplinpro.ui.schedule.EditScheduleScreen
import com.dsp.disiplinpro.ui.task.AddTaskScreen
import com.dsp.disiplinpro.ui.task.AllTasksScreen
import com.dsp.disiplinpro.ui.task.EditTaskScreen
import com.dsp.disiplinpro.ui.theme.Spring
import com.dsp.disiplinpro.viewmodel.auth.AuthViewModel
import com.dsp.disiplinpro.viewmodel.auth.TwoFactorAuthViewModel
import com.dsp.disiplinpro.viewmodel.theme.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DisiplinProNavHost(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    backgroundColor: Color,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        "home"
    } else {
        "onboarding"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.background(backgroundColor)
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

        fun getRouteIndex(route: String?): Int {
            val mainRoutes = mapOf(
                "home" to 0,
                "kalender" to 1,
                "notifikasi" to 2,
                "akun" to 3
            )

            mainRoutes[route]?.let { return it }

            return when {
                route == "list_jadwal" -> 1
                route == "list_tugas" -> 1

                route == "add_jadwal" -> 4
                route == "add_tugas" -> 4

                route?.startsWith("edit_jadwal") == true -> 4
                route?.startsWith("edit_tugas") == true -> 4

                route == "edit_akun" -> 4
                route == "keamanan_privasi" -> 4
                route == "faq" -> 4
                route == "notification_list" -> 4

                route == "forgot_password" -> 4
                route == "email_verification" -> 4
                route == "chatbot" -> 4

                else -> -1
            }
        }

        fun NavGraphBuilder.composableWithAnimations(
            route: String,
            content: @Composable (NavBackStackEntry) -> Unit
        ) {
            composable(
                route = route,
                enterTransition = {
                    val currentIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)

                    val direction = if (targetIndex > currentIndex) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }

                    slideIntoContainer(
                        towards = direction,
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(
                        initialAlpha = 0.3f,
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    val currentIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)

                    val direction = if (targetIndex > currentIndex) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }

                    slideOutOfContainer(
                        towards = direction,
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

        composableWithAnimations("chatbot") {
            ChatbotScreen(navController)
        }
    }
}