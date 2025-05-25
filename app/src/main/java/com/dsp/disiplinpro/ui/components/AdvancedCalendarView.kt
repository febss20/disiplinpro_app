package com.dsp.disiplinpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsp.disiplinpro.data.model.Schedule
import com.dsp.disiplinpro.ui.theme.DarkCardBackground
import com.dsp.disiplinpro.ui.theme.DarkCardLight
import com.dsp.disiplinpro.ui.theme.DarkPrimaryBlue
import com.dsp.disiplinpro.ui.theme.DarkTextLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val DEFAULT_YEAR_RANGE_COUNT = 4
private const val DAYS_IN_WEEK = 7
private const val WEEKS_TO_SHOW = 6
private val DAY_ABBREVIATIONS = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
private val MONTH_NAMES = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

/**
 * Data class menyimpan informasi hari kalender
 */
private data class CalendarDay(
    val date: Calendar,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean,
    val hasSchedule: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedCalendarView(
    currentMonth: Calendar,
    schedules: List<Schedule>,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onMonthChanged: (Calendar) -> Unit,
    isDarkMode: Boolean = false
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val scheduleDays by remember(schedules) {
        derivedStateOf { schedules.map { it.hari.lowercase() }.toSet() }
    }

    var showDatePicker by remember { mutableStateOf(false) }

    val calendarDays by remember(currentMonth, selectedDate, scheduleDays) {
        derivedStateOf { calculateCalendarDays(currentMonth, selectedDate, scheduleDays) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(if (isDarkMode) DarkCardBackground else Color.White, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        CalendarHeader(
            currentMonth = currentMonth,
            monthFormat = monthFormat,
            onPreviousMonth = {
                val newMonth = currentMonth.clone() as Calendar
                newMonth.add(Calendar.MONTH, -1)
                onMonthChanged(newMonth)
            },
            onNextMonth = {
                val newMonth = currentMonth.clone() as Calendar
                newMonth.add(Calendar.MONTH, 1)
                onMonthChanged(newMonth)
            },
            onMonthYearClick = { showDatePicker = true },
            isDarkMode = isDarkMode
        )

        DaysOfWeekHeader(isDarkMode)

        CalendarGrid(calendarDays, onDateSelected, isDarkMode)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentMonth.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        newDate.set(Calendar.DAY_OF_MONTH, 1)
                        onMonthChanged(newDate)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: Calendar,
    monthFormat: SimpleDateFormat,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthYearClick: () -> Unit,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Month",
                tint = if (isDarkMode) DarkTextLight else Color.Black
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDarkMode) DarkCardLight else Color(0xFFE6F1F8))
                .clickable(onClick = onMonthYearClick)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    monthFormat.format(currentMonth.time),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Select Month & Year",
                    tint = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        IconButton(onClick = onNextMonth) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Month",
                tint = if (isDarkMode) DarkTextLight else Color.Black
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader(isDarkMode: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        DAY_ABBREVIATIONS.forEach { day ->
            Text(
                text = day,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = if (isDarkMode) DarkTextLight else Color.Black
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    calendarDays: List<List<CalendarDay?>>,
    onDateSelected: (Calendar) -> Unit,
    isDarkMode: Boolean
) {
    calendarDays.forEach { week ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            week.forEach { day ->
                if (day == null) {
                    Box(modifier = Modifier.weight(1f).height(40.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .background(
                                    when {
                                        day.isSelected -> if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
                                        day.hasSchedule -> if (isDarkMode) Color(0xFF1E4B6B) else Color(0xFFCCE5FF)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    onDateSelected(day.date)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.date.get(Calendar.DAY_OF_MONTH).toString(),
                                color = when {
                                    day.isSelected -> Color.White
                                    isDarkMode -> DarkTextLight
                                    else -> Color.Black
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Menghitung data hari-hari kalender secara efisien
 */
private fun calculateCalendarDays(
    currentMonth: Calendar,
    selectedDate: Calendar,
    scheduleDays: Set<String>
): List<List<CalendarDay?>> {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startingDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
    val indonesianLocale = Locale("id", "ID")
    val dayFormat = SimpleDateFormat("EEEE", indonesianLocale)

    val selectedYear = selectedDate.get(Calendar.YEAR)
    val selectedMonth = selectedDate.get(Calendar.MONTH)
    val selectedDay = selectedDate.get(Calendar.DAY_OF_MONTH)
    val currentMonthValue = currentMonth.get(Calendar.MONTH)
    val currentYearValue = currentMonth.get(Calendar.YEAR)

    val calendarGrid = List(WEEKS_TO_SHOW) { weekIndex ->
        List(DAYS_IN_WEEK) { dayOfWeek ->
            val dayIndex = weekIndex * DAYS_IN_WEEK + dayOfWeek
            val calendarDayIndex = dayIndex - startingDayOfWeek

            if (calendarDayIndex < 0 || calendarDayIndex >= daysInMonth) {
                null
            } else {
                val date = (currentMonth.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, calendarDayIndex + 1)
                }

                val isSelected = selectedDay == (calendarDayIndex + 1) &&
                        selectedMonth == currentMonthValue &&
                        selectedYear == currentYearValue

                val dayOfWeekString = dayFormat.format(date.time).lowercase()
                val hasSchedule = dayOfWeekString in scheduleDays

                CalendarDay(
                    date = date,
                    isCurrentMonth = true,
                    isSelected = isSelected,
                    hasSchedule = hasSchedule
                )
            }
        }
    }

    return calendarGrid
}