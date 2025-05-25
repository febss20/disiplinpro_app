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
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import com.dsp.disiplinpro.ui.theme.Spring
import android.content.Context
import com.dsp.disiplinpro.ui.theme.DarkBackground
import com.dsp.disiplinpro.ui.theme.LightBackground
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import com.dsp.disiplinpro.data.preferences.dataStore

@Composable
fun getBackgroundColorForCurrentTheme(): Color {
    val context = LocalContext.current
    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
    return if (isDarkMode) DarkBackground else LightBackground
}

fun isDarkModeEnabled(context: Context): Boolean {
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    return runBlocking {
        context.dataStore.data.map { preferences ->
            preferences[IS_DARK_MODE] ?: false
        }.first()
    }
}

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
            val weight = 1f

            // Home
            SimpleAnimatedNavItem(
                icon = { color ->
                    Icon(
                        painter = painterResource(R.drawable.vector),
                        contentDescription = "Home",
                        modifier = Modifier
                            .size(22.dp)
                            .scale(1.2f),
                        tint = color
                    )
                },
                label = "Home",
                selected = currentRoute == "home",
                onClick = {
                    if (currentRoute != "home") {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                selectedColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF2196F3),
                unselectedColor = if (isDarkMode) DarkIconInactive else Color(0xFF333333),
                modifier = Modifier.weight(weight)
            )

            // Kalender
            SimpleAnimatedNavItem(
                icon = { color ->
                    Icon(
                        painter = painterResource(R.drawable.kalender),
                        contentDescription = "Kalender",
                        modifier = Modifier
                            .size(22.dp)
                            .scale(1f),
                        tint = color
                    )
                },
                label = "Kalender",
                selected = currentRoute == "kalender",
                onClick = {
                    if (currentRoute != "kalender") {
                        if (currentRoute == "akun") {
                            navController.navigate("kalender") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate("kalender") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                selectedColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF2196F3),
                unselectedColor = if (isDarkMode) DarkIconInactive else Color(0xFF333333),
                modifier = Modifier.weight(weight)
            )

            // Notifikasi
            SimpleAnimatedNavItem(
                icon = { color ->
                    Icon(
                        painter = painterResource(R.drawable.notif),
                        contentDescription = "Notifikasi",
                        modifier = Modifier
                            .size(22.dp)
                            .scale(1.4f),
                        tint = color
                    )
                },
                label = "Notifikasi",
                selected = currentRoute == "notifikasi",
                onClick = {
                    if (currentRoute != "notifikasi") {
                        if (currentRoute == "akun") {
                            navController.navigate("notifikasi") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate("notifikasi") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                selectedColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF2196F3),
                unselectedColor = if (isDarkMode) DarkIconInactive else Color(0xFF333333),
                modifier = Modifier.weight(weight)
            )

            // Akun
            SimpleAnimatedNavItem(
                icon = { color ->
                    Icon(
                        painter = painterResource(R.drawable.akun),
                        contentDescription = "Akun",
                        modifier = Modifier
                            .size(22.dp)
                            .scale(1f),
                        tint = color
                    )
                },
                label = "Akun",
                selected = currentRoute == "akun",
                onClick = {
                    if (currentRoute != "akun") {
                        navController.navigate("akun") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                selectedColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF2196F3),
                unselectedColor = if (isDarkMode) DarkIconInactive else Color(0xFF333333),
                modifier = Modifier.weight(weight)
            )
        }
    }
}

@Composable
fun SimpleAnimatedNavItem(
    icon: @Composable (Color) -> Unit,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selected) {
        if (selected) {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val iconColor = if (selected) selectedColor else unselectedColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                scope.launch {
                    scale.animateTo(
                        targetValue = 0.9f,
                        animationSpec = tween(
                            durationMillis = 50,
                            easing = FastOutSlowInEasing
                        )
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 100,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                onClick()
            }
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
        ) {
            icon(iconColor)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = iconColor
        )
    }
}