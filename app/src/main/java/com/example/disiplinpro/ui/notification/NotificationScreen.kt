package com.example.disiplinpro.ui.notification

import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.disiplinpro.data.preferences.ThemePreferences
import com.example.disiplinpro.ui.components.BottomNavigationBar
import com.example.disiplinpro.ui.theme.DarkBackground
import com.example.disiplinpro.ui.theme.DarkCardBackground
import com.example.disiplinpro.ui.theme.DarkPrimaryBlue
import com.example.disiplinpro.ui.theme.DarkTextLight
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disiplinpro.viewmodel.notification.NotificationViewModel

private const val PREFS_NAME = "NotificationPrefs"
private const val PREF_SCHEDULE_ENABLED = "scheduleNotificationEnabled"
private const val PREF_TASK_ENABLED = "taskNotificationEnabled"
private const val PREF_SCHEDULE_TIME = "scheduleTimeBefore"
private const val PREF_TASK_TIME = "taskTimeBefore"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val timeOptions = listOf("10 Menit sebelum", "30 Menit sebelum", "1 Jam sebelum", "1 Hari sebelum")

    // Cek dark mode
    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val primaryColor = Color(0xFF1E88E5)
    val accentColor = Color(0xFFFFA000)

    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        notificationViewModel.loadNotifications()
    }

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var scheduleNotificationEnabled by remember { mutableStateOf(prefs.getBoolean(PREF_SCHEDULE_ENABLED, false)) }
    var taskNotificationEnabled by remember { mutableStateOf(prefs.getBoolean(PREF_TASK_ENABLED, false)) }
    var scheduleTimeBefore by remember { mutableStateOf(prefs.getString(PREF_SCHEDULE_TIME, "30 Menit sebelum") ?: "30 Menit sebelum") }
    var taskTimeBefore by remember { mutableStateOf(prefs.getString(PREF_TASK_TIME, "1 Jam sebelum") ?: "1 Jam sebelum") }
    var scheduleExpanded by remember { mutableStateOf(false) }
    var taskExpanded by remember { mutableStateOf(false) }
    var showSavedAnimation by remember { mutableStateOf(false) }
    var shouldNavigateBack by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d("NotificationScreen", "Loaded prefs: scheduleEnabled=$scheduleNotificationEnabled, taskEnabled=$taskNotificationEnabled")
    }

    LaunchedEffect(shouldNavigateBack) {
        if (shouldNavigateBack) {
            delay(800)
            navController.popBackStack()
        }
    }

    LaunchedEffect(showSavedAnimation) {
        if (showSavedAnimation) {
            delay(2000)
            showSavedAnimation = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else Color(0xFFFAF3E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 30.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Pengaturan Notifikasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
                )

                Box {
                    IconButton(
                        onClick = { navController.navigate("notification_list") },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Transparent, CircleShape)
                            .padding(start = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Lihat Notifikasi",
                            tint = if (isDarkMode) DarkPrimaryBlue else primaryColor
                        )
                    }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    if (isDarkMode) Color(0xFFE57373) else Color(0xFFFF5252),
                                    CircleShape
                                )
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            NotificationSectionCard(
                title = "Jadwal",
                description = "Dapatkan pengingat tentang jadwal kuliah Anda",
                icon = Icons.Filled.Schedule,
                iconTint = primaryColor,
                isEnabled = scheduleNotificationEnabled,
                timeBefore = scheduleTimeBefore,
                timeOptions = timeOptions,
                isExpanded = scheduleExpanded,
                onExpandChange = { scheduleExpanded = it },
                onToggleChange = { scheduleNotificationEnabled = it },
                onTimeBeforeChange = { scheduleTimeBefore = it },
                isDarkMode = isDarkMode
            )

            NotificationSectionCard(
                title = "Tugas",
                description = "Dapatkan pengingat tentang tenggat waktu tugas Anda",
                icon = Icons.Filled.Task,
                iconTint = accentColor,
                isEnabled = taskNotificationEnabled,
                timeBefore = taskTimeBefore,
                timeOptions = timeOptions,
                isExpanded = taskExpanded,
                onExpandChange = { taskExpanded = it },
                onToggleChange = { taskNotificationEnabled = it },
                onTimeBeforeChange = { taskTimeBefore = it },
                isDarkMode = isDarkMode
            )

            // Disclaimer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    "Catatan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Jika perangkat Anda memiliki fitur pengoptimalan baterai yang agresif, Anda mungkin perlu mengecualikan DisiplinPro dari pengoptimalan baterai untuk memastikan pengingat berfungsi dengan baik.",
                    fontSize = 14.sp,
                    color = if (isDarkMode) DarkTextLight.copy(alpha = 0.7f) else Color(0xFF757575),
                    lineHeight = 20.sp
                )
            }

            // Tombol Simpan
            Button(
                onClick = {
                    with(prefs.edit()) {
                        putBoolean(PREF_SCHEDULE_ENABLED, scheduleNotificationEnabled)
                        putBoolean(PREF_TASK_ENABLED, taskNotificationEnabled)
                        putString(PREF_SCHEDULE_TIME, scheduleTimeBefore)
                        putString(PREF_TASK_TIME, taskTimeBefore)
                        apply()
                    }
                    Log.d("NotificationScreen", "Preferences saved")
                    showSavedAnimation = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7DAFCB)
                )
            ) {
                AnimatedContent(
                    targetState = showSavedAnimation,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    }
                ) { saved ->
                    if (saved) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tersimpan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            "Simpan Pengaturan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        BottomNavigationBar(
            navController = navController,
            currentRoute = currentRoute,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationSectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    isEnabled: Boolean,
    timeBefore: String,
    timeOptions: List<String>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onToggleChange: (Boolean) -> Unit,
    onTimeBeforeChange: (String) -> Unit,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) DarkCardBackground else Color(0x332196F3)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
                        )
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = if (isDarkMode) DarkTextLight.copy(alpha = 0.7f) else Color(0xFF757575)
                        )
                    }
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (isDarkMode) Color.White else Color.White,
                        checkedTrackColor = if (isDarkMode) DarkPrimaryBlue else iconTint,
                        uncheckedThumbColor = if (isDarkMode) DarkTextLight.copy(alpha = 0.5f) else Color.LightGray,
                        uncheckedTrackColor = if (isDarkMode) DarkCardBackground.copy(alpha = 0.5f) else Color(0xFFDDDDDD)
                    )
                )
            }

            if (isEnabled) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = if (isDarkMode) Color(0xFF333333) else Color(0xFFEEEEEE),
                    thickness = 1.dp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Waktu pengingat",
                        fontSize = 16.sp,
                        color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
                    )

                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onExpandChange(!isExpanded) }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = timeBefore,
                                fontSize = 16.sp,
                                color = if (isDarkMode) DarkPrimaryBlue else iconTint
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Pilih Waktu",
                                tint = if (isDarkMode) DarkPrimaryBlue else iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { onExpandChange(false) },
                            modifier = Modifier.background(if (isDarkMode) DarkCardBackground else Color.White)
                        ) {
                            timeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
                                        )
                                    },
                                    onClick = {
                                        onTimeBeforeChange(option)
                                        onExpandChange(false)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsOff,
                            contentDescription = "Notifikasi Dimatikan",
                            tint = if (isDarkMode) DarkTextLight.copy(alpha = 0.5f) else Color(0xFFAAAAAA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notifikasi dimatikan",
                            fontSize = 14.sp,
                            color = if (isDarkMode) DarkTextLight.copy(alpha = 0.5f) else Color(0xFFAAAAAA),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}