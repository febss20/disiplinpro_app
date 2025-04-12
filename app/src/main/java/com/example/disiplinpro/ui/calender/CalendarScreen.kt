package com.example.disiplinpro.ui.calender

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.ui.components.AdvancedCalendarView
import com.example.disiplinpro.ui.components.BottomNavigationBar
import com.example.disiplinpro.ui.components.ScheduleDescription
import com.example.disiplinpro.viewmodel.schedule.ScheduleViewModel
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
            .background(Color(0xFFFAF3E0))
    ) {
        Text(
            "Kalender",
            color = Color(0xFF7DAFCB),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            textAlign = TextAlign.Center
        )

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
                    onMonthChanged = { newMonth -> currentMonth = newMonth }
                )

                if (schedules.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                    Text(
                        "Memuat jadwal...",
                        color = Color(0xFF333333),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                } else if (selectedSchedules.isNotEmpty()) {
                    ScheduleDescription(
                        schedules = selectedSchedules,
                        selectedDate = selectedDate,
                        timeFormat = timeFormat,
                        onDelete = { scheduleId -> viewModel.deleteSchedule(scheduleId) }
                    )
                } else {
                    Text(
                        "Tidak ada jadwal pada tanggal ini",
                        color = Color(0xFF333333),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            BottomNavigationBar(navController = navController, currentRoute = "kalender")
        }
    }

    LaunchedEffect(schedules) {
        println("Jadwal yang ditampilkan: $schedules")
    }
}