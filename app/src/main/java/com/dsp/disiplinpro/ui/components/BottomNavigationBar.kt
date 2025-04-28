package com.dsp.disiplinpro.ui.components

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
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.DarkCardBackground
import com.dsp.disiplinpro.ui.theme.DarkIconInactive
import com.dsp.disiplinpro.ui.theme.DarkPrimaryBlue
import com.dsp.disiplinpro.R

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
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
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
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
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
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
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
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
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
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