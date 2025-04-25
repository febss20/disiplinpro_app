package com.example.disiplinpro.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.disiplinpro.R
import com.example.disiplinpro.data.preferences.ThemePreferences
import com.example.disiplinpro.ui.theme.DarkCardBackground
import com.example.disiplinpro.ui.theme.DarkIconInactive
import com.example.disiplinpro.ui.theme.DarkPrimaryBlue
import com.example.disiplinpro.ui.theme.DarkTextLight

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    if (currentRoute != null && currentRoute != "home") {
        BackHandler {
            navController.navigate("home") {
                popUpTo("home") {
                    inclusive = false
                }
            }
        }
    }

    // Cek apakah dark mode aktif
    val context = LocalContext.current
    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 17.dp)
            .height(80.dp),
        shape = RoundedCornerShape(50.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = if (isDarkMode) DarkCardBackground else Color(0xFFFFF8E1)
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.vector),
                            contentDescription = "Home",
                            modifier = Modifier
                                .size(22.dp)
                                .scale(1.2f)
                        )
                    },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = {
                        if (currentRoute != "home") {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF2196F3),
                        unselectedIconColor = if (isDarkMode) DarkIconInactive else Color(0xFF333333),
                        selectedTextColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.kalender),
                            contentDescription = "Kalender",
                            modifier = Modifier
                                .size(22.dp)
                                .scale(1f)
                        )
                    },
                    label = { Text("Kalender") },
                    selected = currentRoute == "kalender",
                    onClick = {
                        if (currentRoute != "kalender") {
                            navController.navigate("kalender") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF2196F3),
                        unselectedIconColor = if (isDarkMode) DarkIconInactive else Color(0xFF333333),
                        selectedTextColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.notif),
                            contentDescription = "Notifikasi",
                            modifier = Modifier
                                .size(22.dp)
                                .scale(1.4f)
                        )
                    },
                    label = { Text("Notifikasi") },
                    selected = currentRoute == "notifikasi",
                    onClick = {
                        if (currentRoute != "notifikasi") {
                            navController.navigate("notifikasi") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF2196F3),
                        unselectedIconColor = if (isDarkMode) DarkIconInactive else Color(0xFF333333),
                        selectedTextColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.akun),
                            contentDescription = "Akun",
                            modifier = Modifier
                                .size(22.dp)
                                .scale(1f)
                        )
                    },
                    label = { Text("Akun") },
                    selected = currentRoute == "akun",
                    onClick = {
                        if (currentRoute != "akun") {
                            navController.navigate("akun") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF2196F3),
                        unselectedIconColor = if (isDarkMode) DarkIconInactive else Color(0xFF333333),
                        selectedTextColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
                    )
                )
            }
        }
    }
}