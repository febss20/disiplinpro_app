package com.example.disiplinpro.ui.notification

import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.disiplinpro.ui.components.BottomNavigationBar
import kotlinx.coroutines.delay

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

    val backgroundColor = Color(0xFFFAF3E0)
    val primaryColor = Color(0xFF1E88E5)
    val accentColor = Color(0xFFFFA000)

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
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 150.dp, start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Pengaturan Notifikasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF333333)
                )

                IconButton(
                    onClick = { navController.navigate("notification_history") },
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Lihat Riwayat Notifikasi",
                        tint = primaryColor
                    )
                }
            }

            // Schedule Notification Card
            NotificationSettingCard(
                title = "Notifikasi Jadwal Kuliah",
                icon = Icons.Filled.Schedule,
                iconTint = primaryColor,
                isEnabled = scheduleNotificationEnabled,
                onEnabledChange = { scheduleNotificationEnabled = it },
                disabledMessage = "Notifikasi jadwal dinonaktifkan",
                timeBefore = scheduleTimeBefore,
                isExpanded = scheduleExpanded,
                onExpandedChange = { scheduleExpanded = it },
                timeOptions = timeOptions,
                onTimeSelected = { scheduleTimeBefore = it },
                switchColors = SwitchDefaults.colors(
                    checkedThumbColor = primaryColor,
                    checkedTrackColor = primaryColor.copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                ),
                textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                ),
                iconTintForSelectedItem = primaryColor
            )

            // Task Notification Card
            NotificationSettingCard(
                title = "Notifikasi Tugas",
                icon = Icons.Filled.Task,
                iconTint = accentColor,
                isEnabled = taskNotificationEnabled,
                onEnabledChange = { taskNotificationEnabled = it },
                disabledMessage = "Notifikasi tugas dinonaktifkan",
                timeBefore = taskTimeBefore,
                isExpanded = taskExpanded,
                onExpandedChange = { taskExpanded = it },
                timeOptions = timeOptions,
                onTimeSelected = { taskTimeBefore = it },
                switchColors = SwitchDefaults.colors(
                    checkedThumbColor = accentColor,
                    checkedTrackColor = accentColor.copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                ),
                textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                ),
                iconTintForSelectedItem = accentColor
            )
        }

        // Fixed Save Button container
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 120.dp)
        ) {
            Button(
                onClick = {
                    showSavedAnimation = true

                    with(prefs.edit()) {
                        putBoolean(PREF_SCHEDULE_ENABLED, scheduleNotificationEnabled)
                        putBoolean(PREF_TASK_ENABLED, taskNotificationEnabled)
                        putString(PREF_SCHEDULE_TIME, scheduleTimeBefore)
                        putString(PREF_TASK_TIME, taskTimeBefore)
                        apply()
                    }
                    Log.d("NotificationScreen", "Saved prefs: scheduleEnabled=$scheduleNotificationEnabled, taskEnabled=$taskNotificationEnabled")

                    shouldNavigateBack = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showSavedAnimation) Color(0xFF66BB4F) else Color(0xFF7DAFCB)
                ),
                shape = RoundedCornerShape(10.dp)
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
private fun NotificationSettingCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    disabledMessage: String,
    timeBefore: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    timeOptions: List<String>,
    onTimeSelected: (String) -> Unit,
    switchColors: SwitchColors,
    textFieldColors: TextFieldColors,
    iconTintForSelectedItem: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x332196F3)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(28.dp)
                        .background(iconTint.copy(alpha = 0.1f), CircleShape)
                        .padding(4.dp)
                )
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChange,
                    colors = switchColors
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.7f)
            )

            // Conditional content based on enabled state
            if (isEnabled) {
                NotificationEnabledContent(
                    timeBefore = timeBefore,
                    isExpanded = isExpanded,
                    onExpandedChange = onExpandedChange,
                    timeOptions = timeOptions,
                    onTimeSelected = onTimeSelected,
                    textFieldColors = textFieldColors,
                    iconTintForSelectedItem = iconTintForSelectedItem
                )
            } else {
                NotificationDisabledMessage(message = disabledMessage)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationEnabledContent(
    timeBefore: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    timeOptions: List<String>,
    onTimeSelected: (String) -> Unit,
    textFieldColors: TextFieldColors,
    iconTintForSelectedItem: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            "Waktu Pengingat",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { onExpandedChange(!isExpanded) },
        ) {
            OutlinedTextField(
                value = timeBefore,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown Menu"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                colors = textFieldColors
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(Color(0x332196F3))
            ) {
                timeOptions.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            onTimeSelected(time)
                            onExpandedChange(false)
                        },
                        trailingIcon = {
                            if (timeBefore == time) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = iconTintForSelectedItem
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationDisabledMessage(message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsOff,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Text(
            message,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}