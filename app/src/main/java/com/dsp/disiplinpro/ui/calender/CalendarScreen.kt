package com.dsp.disiplinpro.ui.calender

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.components.AdvancedCalendarView
import com.dsp.disiplinpro.ui.components.ScheduleDescription
import com.dsp.disiplinpro.ui.components.StandardHeader
import com.dsp.disiplinpro.ui.theme.DarkBackground
import com.dsp.disiplinpro.ui.theme.DarkTextLight
import com.dsp.disiplinpro.viewmodel.schedule.ScheduleViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = viewModel()
) {
    val schedules by viewModel.schedules.collectAsState(initial = emptyList())
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val context = LocalContext.current
    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    if (FirebaseAuth.getInstance().currentUser == null) {
        Text(
            "Silakan login untuk melihat jadwal",
            color = Color.Red,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
        LaunchedEffect(Unit) {
            navController.navigate("login")
        }
        return
    }

    // Filter jadwal berdasarkan field 'hari'
    val selectedSchedules = schedules.filter { schedule ->
        val selectedDay = SimpleDateFormat("EEEE", Locale("id", "ID")).format(selectedDate.time).lowercase()
        println("Selected day: $selectedDay, Schedule hari: ${schedule.hari.lowercase()}")
        schedule.hari.lowercase() == selectedDay
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else Color(0xFFFAF3E0))
    ) {
        StandardHeader(title = "Kalender")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp)
                .padding(top = 55.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                AdvancedCalendarView(
                    currentMonth = currentMonth,
                    schedules = schedules,
                    selectedDate = selectedDate,
                    onDateSelected = { newDate -> selectedDate = newDate },
                    onMonthChanged = { newMonth -> currentMonth = newMonth },
                    isDarkMode = isDarkMode
                )

                Text(
                    "Jadwal Hari ${SimpleDateFormat("EEEE", Locale("id", "ID")).format(selectedDate.time)}",
                    color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 12.dp)
                )

                if (selectedSchedules.isEmpty()) {
                    Text(
                        "Tidak ada jadwal pada hari ini",
                        color = if (isDarkMode) DarkTextLight.copy(alpha = 0.7f) else Color(0xFF757575),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    ScheduleDescription(
                        schedules = selectedSchedules,
                        selectedDate = selectedDate,
                        timeFormat = timeFormat,
                        isDarkMode = isDarkMode,
                        onDelete = { scheduleId -> viewModel.deleteSchedule(scheduleId) }
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    LaunchedEffect(schedules) {
        println("Jadwal yang ditampilkan: $schedules")
    }
}