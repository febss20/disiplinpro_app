package com.example.disiplinpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disiplinpro.data.model.Schedule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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