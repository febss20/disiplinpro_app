package com.example.disiplinpro.ui.notification

import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.disiplinpro.ui.components.BottomNavigationBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    // State variables for notification settings
    var scheduleNotificationEnabled by remember { mutableStateOf(false) }
    var taskNotificationEnabled by remember { mutableStateOf(false) }
    var scheduleTimeBefore by remember { mutableStateOf("30 Menit") }
    var taskTimeBefore by remember { mutableStateOf("1 Jam") }
    var scheduleExpanded by remember { mutableStateOf(false) }
    var taskExpanded by remember { mutableStateOf(false) }
    var showSavedAnimation by remember { mutableStateOf(false) }

    // Animation properties
    val cardElevation by animateDpAsState(
        targetValue = if (showSavedAnimation) 8.dp else 4.dp,
        animationSpec = tween(durationMillis = 300)
    )

    // Load saved preferences when the screen is opened
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        scheduleNotificationEnabled = prefs.getBoolean("scheduleNotificationEnabled", false)
        taskNotificationEnabled = prefs.getBoolean("taskNotificationEnabled", false)
        scheduleTimeBefore = prefs.getString("scheduleTimeBefore", "30 Menit") ?: "30 Menit"
        taskTimeBefore = prefs.getString("taskTimeBefore", "1 Jam") ?: "1 Jam"
        Log.d("NotificationScreen", "Loaded prefs: scheduleEnabled=$scheduleNotificationEnabled, taskEnabled=$taskNotificationEnabled")
    }

    val timeOptions = listOf("10 Menit sebelum", "30 Menit sebelum", "1 Jam sebelum", "1 Hari sebelum")

    val backgroundColor = Color(0xFFFAF3E0)
    val primaryColor = Color(0xFF1E88E5)
    val accentColor = Color(0xFFFFA000)

    // Variable to track if we should navigate back
    var shouldNavigateBack by remember { mutableStateOf(false) }

    // Effect to handle navigation after animation completes
    LaunchedEffect(shouldNavigateBack) {
        if (shouldNavigateBack) {
            delay(800)
            navController.popBackStack()
        }
    }

    // Effect to reset the animation state
    LaunchedEffect(showSavedAnimation) {
        if (showSavedAnimation) {
            delay(2000) // Wait for 2 seconds before resetting
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
                .padding(top = 40.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título principal
            Text(
                "Pengaturan Notifikasi",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Jadwal Kuliah Card
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
                    // Card Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier
                                .size(28.dp)
                                .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                                .padding(4.dp)
                        )
                        Text(
                            "Notifikasi Jadwal Kuliah",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = scheduleNotificationEnabled,
                            onCheckedChange = { scheduleNotificationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = primaryColor,
                                checkedTrackColor = primaryColor.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.LightGray.copy(alpha = 0.7f)
                    )

                    // Dropdown section, only visible when notifications are enabled
                    AnimatedVisibility(
                        visible = scheduleNotificationEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
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
                                expanded = scheduleExpanded,
                                onExpandedChange = { scheduleExpanded = !scheduleExpanded },
                            ) {
                                OutlinedTextField(
                                    value = scheduleTimeBefore,
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
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = scheduleExpanded,
                                    onDismissRequest = { scheduleExpanded = false },
                                    modifier = Modifier.background(Color(0x332196F3))
                                ) {
                                    timeOptions.forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text(time) },
                                            onClick = {
                                                scheduleTimeBefore = time
                                                scheduleExpanded = false
                                            },
                                            trailingIcon = {
                                                if (scheduleTimeBefore == time) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = "Selected",
                                                        tint = primaryColor
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Message when disabled
                    AnimatedVisibility(
                        visible = !scheduleNotificationEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
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
                                "Notifikasi jadwal dinonaktifkan",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Daftar Tugas Card
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
                    // Card Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Task,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .size(28.dp)
                                .background(accentColor.copy(alpha = 0.1f), CircleShape)
                                .padding(4.dp)
                        )
                        Text(
                            "Notifikasi Tugas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = taskNotificationEnabled,
                            onCheckedChange = { taskNotificationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = accentColor,
                                checkedTrackColor = accentColor.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.LightGray.copy(alpha = 0.7f)
                    )

                    // Dropdown section, only visible when notifications are enabled
                    AnimatedVisibility(
                        visible = taskNotificationEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
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
                                expanded = taskExpanded,
                                onExpandedChange = { taskExpanded = !taskExpanded },
                            ) {
                                OutlinedTextField(
                                    value = taskTimeBefore,
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
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentColor,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = taskExpanded,
                                    onDismissRequest = { taskExpanded = false },
                                    modifier = Modifier.background(Color(0x332196F3))
                                ) {
                                    timeOptions.forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text(time) },
                                            onClick = {
                                                taskTimeBefore = time
                                                taskExpanded = false
                                            },
                                            trailingIcon = {
                                                if (taskTimeBefore == time) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = "Selected",
                                                        tint = accentColor
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Message when disabled
                    AnimatedVisibility(
                        visible = !taskNotificationEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
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
                                "Notifikasi tugas dinonaktifkan",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(bottom = 82.dp))

            // Save Button
            Button(
                onClick = {
                    // Show animation
                    showSavedAnimation = true

                    // Save preferences
                    val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
                    with(prefs.edit()) {
                        putBoolean("scheduleNotificationEnabled", scheduleNotificationEnabled)
                        putBoolean("taskNotificationEnabled", taskNotificationEnabled)
                        putString("scheduleTimeBefore", scheduleTimeBefore)
                        putString("taskTimeBefore", taskTimeBefore)
                        apply()
                    }
                    Log.d("NotificationScreen", "Saved prefs: scheduleEnabled=$scheduleNotificationEnabled, taskEnabled=$taskNotificationEnabled")

                    // Set flag to navigate back after delay
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

            // Agregar espacio para el BottomNavigationBar
            Spacer(modifier = Modifier.height(80.dp))
        }

        // BottomNavigationBar colocado manualmente con un margen inferior
        BottomNavigationBar(
            navController = navController,
            currentRoute = currentRoute,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp) // Agregar un padding inferior para elevarlo
        )
    }
}