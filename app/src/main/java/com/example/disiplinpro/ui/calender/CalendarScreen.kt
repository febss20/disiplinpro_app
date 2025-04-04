package com.example.disiplinpro.ui.calender

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.ui.components.BottomNavigationBar
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Kalender",
                        color = Color(0xFF333333),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF3E0))
            )

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

@Composable
fun AdvancedCalendarView(
    currentMonth: Calendar,
    schedules: List<Schedule>,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onMonthChanged: (Calendar) -> Unit
) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = currentMonth.apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startingDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Tanggal yang memiliki jadwal berdasarkan field 'hari'
    val scheduleDays = schedules.map { it.hari.lowercase() }.toSet()
    val selectedDay = SimpleDateFormat("EEEE", Locale("id", "ID")).format(selectedDate.time).lowercase()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newMonth = currentMonth.clone() as Calendar
                newMonth.add(Calendar.MONTH, -1)
                onMonthChanged(newMonth)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }
            Text(
                monthFormat.format(currentMonth.time),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                val newMonth = currentMonth.clone() as Calendar
                newMonth.add(Calendar.MONTH, 1)
                onMonthChanged(newMonth)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab").forEach { day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        var dayCounter = 1
        for (week in 0..5) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    if (week == 0 && dayOfWeek < startingDayOfWeek || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(40.dp))
                    } else {
                        val currentDate = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, dayCounter)
                            set(Calendar.MONTH, currentMonth.get(Calendar.MONTH))
                            set(Calendar.YEAR, currentMonth.get(Calendar.YEAR))
                        }
                        val isSelected = selectedDate.get(Calendar.DAY_OF_MONTH) == dayCounter &&
                                selectedDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                selectedDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                        val currentDay = SimpleDateFormat("EEEE", Locale("id", "ID")).format(currentDate.time).lowercase()
                        val hasSchedule = currentDay in scheduleDays

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> Color(0xFF7DAFCB)
                                        hasSchedule -> Color(0xFFCCE5FF)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    onDateSelected(currentDate)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayCounter.toString(),
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 14.sp
                            )
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleDescription(
    schedules: List<Schedule>,
    selectedDate: Calendar,
    timeFormat: SimpleDateFormat,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 16.dp)
    ) {
        schedules.forEach { schedule ->
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x1A2196F3))
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time),
                        color = Color(0xFF7DAFCB),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Delete",
                        color = Color(0xFFFF5722),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            schedule.id.let { onDelete(it) }
                        }
                    )
                }
                Text(
                    timeFormat.format(schedule.waktuMulai.toDate()),
                    color = Color(0xFF333333),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    schedule.ruangan,
                    color = Color(0xFF333333),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    schedule.matkul,
                    color = Color(0xFF333333),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}