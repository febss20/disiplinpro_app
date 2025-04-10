package com.example.disiplinpro.ui.notification

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.disiplinpro.ui.components.BottomNavigationBar
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    var scheduleNotificationEnabled by remember { mutableStateOf(false) }
    var taskNotificationEnabled by remember { mutableStateOf(false) }
    var scheduleTimeBefore by remember { mutableStateOf("30 Menit") }
    var taskTimeBefore by remember { mutableStateOf("1 Jam") }
    var scheduleExpanded by remember { mutableStateOf(false) }
    var taskExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        scheduleNotificationEnabled = prefs.getBoolean("scheduleNotificationEnabled", false)
        taskNotificationEnabled = prefs.getBoolean("taskNotificationEnabled", false)
        scheduleTimeBefore = prefs.getString("scheduleTimeBefore", "30 Menit") ?: "30 Menit"
        taskTimeBefore = prefs.getString("taskTimeBefore", "1 Jam") ?: "1 Jam"
    }

    val timeOptions = listOf("10 Menit", "30 Menit", "1 Jam", "1 Hari")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFFAF3E0), RoundedCornerShape(20.dp))
            ) {
                Text(
                    "Atur Notifikasi",
                    color = Color(0xFF333333),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 45.dp, bottom = 24.dp, start = 30.dp)
                )

                // Card untuk Jadwal Kuliah
                Column(
                    modifier = Modifier
                        .padding(bottom = 24.dp, start = 30.dp, end = 30.dp)
                        .border(1.dp, Color(0x4D333333), RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .background(Color(0x1A2196F3), RoundedCornerShape(10.dp))
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        "Jadwal Kuliah",
                        color = Color(0xFF333333),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp, start = 19.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 19.dp, vertical = 22.dp)
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(Color(0xFF000000))
                    ) {}
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 19.dp)
                            .fillMaxWidth()
                    ) {
                        CoilImage(
                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/285pk63d_expires_30_days.png" },
                            imageOptions = ImageOptions(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                            modifier = Modifier
                                .padding(end = 7.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .width(21.dp)
                                .height(20.dp)
                        )
                        if (scheduleNotificationEnabled) {
                            ExposedDropdownMenuBox(
                                expanded = scheduleExpanded,
                                onExpandedChange = { scheduleExpanded = !scheduleExpanded },
                                modifier = Modifier
                                    .padding(end = 13.dp)
                                    .fillMaxWidth(0.7f)
                                    .border(1.dp, Color(0x4D333333), RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x332196F3))
                            ) {
                                TextField(
                                    value = scheduleTimeBefore,
                                    onValueChange = {},
                                    readOnly = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF333333), fontSize = 16.sp),
                                    trailingIcon = {
                                        CoilImage(
                                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/ak1ar00d_expires_30_days.png" },
                                            imageOptions = ImageOptions(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                                            modifier = Modifier
                                                .padding(end = 5.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .width(29.dp)
                                                .height(29.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .background(Color.Transparent)
                                )
                                ExposedDropdownMenu(
                                    expanded = scheduleExpanded,
                                    onDismissRequest = { scheduleExpanded = false },
                                    modifier = Modifier.background(Color(0x1A2196F3))
                                ) {
                                    timeOptions.forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text(time, color = Color(0xFF333333), fontSize = 16.sp) },
                                            onClick = {
                                                scheduleTimeBefore = time
                                                scheduleExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Switch(
                            checked = scheduleNotificationEnabled,
                            onCheckedChange = { scheduleNotificationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF7DAFCB),
                                checkedTrackColor = Color(0xFF7DAFCB).copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // Card untuk Daftar Tugas
                Column(
                    modifier = Modifier
                        .padding(bottom = 24.dp, start = 30.dp, end = 30.dp)
                        .border(1.dp, Color(0x4D333333), RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .background(Color(0x1A2196F3), RoundedCornerShape(10.dp))
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        "Daftar Tugas",
                        color = Color(0xFF333333),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 19.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 19.dp, vertical = 22.dp)
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(Color(0xFF000000))
                    ) {}
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 19.dp)
                            .fillMaxWidth()
                    ) {
                        CoilImage(
                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/9kchx17p_expires_30_days.png" },
                            imageOptions = ImageOptions(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                            modifier = Modifier
                                .padding(end = 7.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .width(21.dp)
                                .height(20.dp)
                        )
                        if (taskNotificationEnabled) {
                            ExposedDropdownMenuBox(
                                expanded = taskExpanded,
                                onExpandedChange = { taskExpanded = !taskExpanded },
                                modifier = Modifier
                                    .padding(end = 13.dp)
                                    .fillMaxWidth(0.7f)
                                    .border(1.dp, Color(0x4D333333), RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x332196F3))
                            ) {
                                TextField(
                                    value = taskTimeBefore,
                                    onValueChange = {},
                                    readOnly = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF333333), fontSize = 16.sp),
                                    trailingIcon = {
                                        CoilImage(
                                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/ak1ar00d_expires_30_days.png" },
                                            imageOptions = ImageOptions(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                                            modifier = Modifier
                                                .padding(end = 5.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .width(29.dp)
                                                .height(29.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .background(Color.Transparent)
                                )
                                ExposedDropdownMenu(
                                    expanded = taskExpanded,
                                    onDismissRequest = { taskExpanded = false },
                                    modifier = Modifier.background(Color(0x1A2196F3))
                                ) {
                                    timeOptions.forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text(time, color = Color(0xFF333333), fontSize = 16.sp) },
                                            onClick = {
                                                taskTimeBefore = time
                                                taskExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Switch(
                            checked = taskNotificationEnabled,
                            onCheckedChange = { taskNotificationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF7DAFCB),
                                checkedTrackColor = Color(0xFF7DAFCB).copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
                        with(prefs.edit()) {
                            putBoolean("scheduleNotificationEnabled", scheduleNotificationEnabled)
                            putBoolean("taskNotificationEnabled", taskNotificationEnabled)
                            putString("scheduleTimeBefore", scheduleTimeBefore)
                            putString("taskTimeBefore", taskTimeBefore)
                            apply()
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 30.dp, end = 30.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DAFCB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simpan", color = Color.White, fontSize = 20.sp)
                }
            }

            // BottomNavigationBar diletakkan di luar Column utama, seperti di CalendarScreen
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    }
}