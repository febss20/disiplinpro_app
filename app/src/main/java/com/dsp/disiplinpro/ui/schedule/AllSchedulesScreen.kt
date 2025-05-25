package com.dsp.disiplinpro.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dsp.disiplinpro.data.model.Schedule
import com.dsp.disiplinpro.ui.theme.*
import com.dsp.disiplinpro.viewmodel.schedule.ScheduleViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.ui.platform.LocalContext
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSchedulesScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = viewModel()
) {
    val context = LocalContext.current

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val secondaryTextColor = if (isDarkMode) DarkTextGrey else Color(0xFF757575)
    val accentColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
    val accentColorDisabled = if (isDarkMode) DarkPrimaryBlue.copy(alpha = 0.5f) else Color(0xFF7DAFCB).copy(alpha = 0.5f)
    val deleteColor = if (isDarkMode) Color(0xFFFF5252) else Color(0xFFD86F6F)
    val deleteColorDisabled = if (isDarkMode) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFFD86F6F).copy(alpha = 0.5f)

    val schedules by viewModel.schedules.collectAsState(initial = emptyList())
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    val scrollState = rememberScrollState()

    val dayOrder = listOf("senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "minggu")

    // Mengurutkan schedules
    val sortedSchedules = schedules.sortedWith(compareBy(
        { dayOrder.indexOf(it.hari.lowercase()) },
        { it.waktuMulai.toDate().time }
    ))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp)
        ) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 30.dp)
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                        Text(
                            "Semua Jadwal",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                selectedSchedule?.let { schedule ->
                                    navController.navigate("edit_jadwal/${schedule.id}")
                                }
                            },
                            enabled = selectedSchedule != null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedSchedule != null) accentColor else accentColorDisabled
                                )
                        ) {
                            CoilImage(
                                imageModel = { R.drawable.edit },
                                modifier = Modifier.size(24.dp),
                                imageOptions = ImageOptions(contentScale = ContentScale.Fit)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = {
                                selectedSchedule?.let { schedule ->
                                    schedule.id.let { id ->
                                        viewModel.deleteSchedule(id)
                                        selectedSchedule = null
                                    }
                                }
                            },
                            enabled = selectedSchedule != null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedSchedule != null) deleteColor else deleteColorDisabled
                                )
                        ) {
                            CoilImage(
                                imageModel = { R.drawable.delete },
                                modifier = Modifier.size(24.dp),
                                imageOptions = ImageOptions(contentScale = ContentScale.Fit)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (sortedSchedules.isEmpty()) {
                    // Empty state message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Empty Schedule",
                            modifier = Modifier.size(120.dp),
                            tint = accentColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada jadwal",
                            color = accentColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tambahkan jadwal baru dengan tombol di bawah",
                            color = secondaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        sortedSchedules.forEach { schedule ->
                            ScheduleItem(
                                schedules = listOf(schedule),
                                isSelected = selectedSchedule == schedule,
                                modifier = Modifier
                                    .clickable {
                                        selectedSchedule = if (selectedSchedule == schedule) null else schedule
                                    }
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                // Tombol Tambah Jadwal
                FloatingActionButton(
                    onClick = { navController.navigate("add_jadwal") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 25.dp, end = 30.dp),
                    containerColor = accentColor,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Jadwal",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}