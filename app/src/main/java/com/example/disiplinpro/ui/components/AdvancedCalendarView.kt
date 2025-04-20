package com.example.disiplinpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.disiplinpro.data.model.Schedule
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

@Composable
fun AdvancedCalendarView(
    currentMonth: Calendar,
    schedules: List<Schedule>,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onMonthChanged: (Calendar) -> Unit
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val scheduleDays by remember(schedules) {
        derivedStateOf { schedules.map { it.hari.lowercase() }.toSet() }
    }

    var showYearRangeDialog by remember { mutableStateOf(false) }
    var showMonthSelectionDialog by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableIntStateOf(currentMonth.get(Calendar.YEAR)) }

    val calendarDays by remember(currentMonth, selectedDate, scheduleDays) {
        derivedStateOf { calculateCalendarDays(currentMonth, selectedDate, scheduleDays) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
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
            onMonthYearClick = { showYearRangeDialog = true }
        )

        DaysOfWeekHeader()

        CalendarGrid(calendarDays, onDateSelected)
    }

    if (showYearRangeDialog) {
        YearRangeSelectionDialog(
            onDismiss = { showYearRangeDialog = false },
            onYearRangeSelected = { year ->
                selectedYear = year
                showYearRangeDialog = false
                showMonthSelectionDialog = true
            }
        )
    }

    if (showMonthSelectionDialog) {
        MonthSelectionDialog(
            selectedYear = selectedYear,
            onDismiss = { showMonthSelectionDialog = false },
            onBackPressed = {
                showMonthSelectionDialog = false
                showYearRangeDialog = true
            },
            onMonthSelected = { month ->
                showMonthSelectionDialog = false
                val newDate = currentMonth.clone() as Calendar
                newDate.set(Calendar.YEAR, selectedYear)
                newDate.set(Calendar.MONTH, month)
                onMonthChanged(newDate)
            }
        )
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: Calendar,
    monthFormat: SimpleDateFormat,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthYearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE6F1F8))
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
                    color = Color(0xFF7DAFCB)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Select Month & Year",
                    tint = Color(0xFF7DAFCB),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DAY_ABBREVIATIONS.forEach { day ->
            Text(
                text = day,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(calendarDays: List<List<CalendarDay?>>, onDateSelected: (Calendar) -> Unit) {
    calendarDays.forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { day ->
                if (day == null) {
                    Box(modifier = Modifier.weight(1f).height(40.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    day.isSelected -> Color(0xFF7DAFCB)
                                    day.hasSchedule -> Color(0xFFCCE5FF)
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
                            color = if (day.isSelected) Color.White else Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun YearRangeSelectionDialog(
    onDismiss: () -> Unit,
    onYearRangeSelected: (Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val baseYear = ((currentYear - 5) / 5) * 5

    val yearRanges by remember(baseYear) {
        derivedStateOf {
            (0 until DEFAULT_YEAR_RANGE_COUNT).map { i ->
                val start = baseYear + (i * 5)
                start to start + 4
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pilih Rentang Tahun",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color(0xFF7DAFCB)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(yearRanges) { (start, end) ->
                        val isCurrentRange = currentYear in start..end
                        Text(
                            text = "$start–$end",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = if (isCurrentRange) Color.White else Color.Black,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCurrentRange) Color(0xFF7DAFCB) else Color(0xFFE0E0E0)
                                )
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                .fillMaxWidth()
                                .clickable {
                                    onYearRangeSelected(start)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelectionDialog(
    selectedYear: Int,
    onDismiss: () -> Unit,
    onBackPressed: () -> Unit,
    onMonthSelected: (Int) -> Unit
) {
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke pilihan tahun",
                            tint = Color(0xFF7DAFCB)
                        )
                    }

                    Text(
                        text = "Tahun: $selectedYear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF7DAFCB)
                    )

                    Box(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(MONTH_NAMES.indices.toList()) { index ->
                        val isCurrentMonth = index == currentMonth && selectedYear == currentYear
                        Text(
                            text = MONTH_NAMES[index],
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = if (isCurrentMonth) Color.White else Color.Black,
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCurrentMonth) Color(0xFF7DAFCB) else Color(0xFFE0E0E0)
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .fillMaxWidth()
                                .clickable {
                                    onMonthSelected(index)
                                }
                        )
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