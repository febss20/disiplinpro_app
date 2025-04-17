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
import androidx.compose.ui.window.Dialog
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

    // State for dialog visibility
    var showYearRangeDialog by remember { mutableStateOf(false) }
    var showMonthSelectionDialog by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(currentMonth.get(Calendar.YEAR)) }

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

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE6F1F8))
                    .clickable { showYearRangeDialog = true }
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
                    textAlign = TextAlign.Center
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

    // Show Year Range Dialog
    if (showYearRangeDialog) {
        YearRangeSelectionDialog(
            onDismiss = { showYearRangeDialog = false },
            onYearRangeSelected = { selectedYearRange ->
                showYearRangeDialog = false
                showMonthSelectionDialog = true
                selectedYear = selectedYearRange
            }
        )
    }

    // Show Month Selection Dialog
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
fun YearRangeSelectionDialog(
    onDismiss: () -> Unit,
    onYearRangeSelected: (Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    // Calculate ranges to include current year in the middle
    val baseYear = (currentYear / 5) * 5 - 5  // Round to nearest 5 and subtract 5
    val yearRanges = listOf(
        baseYear to baseYear + 4,
        baseYear + 5 to baseYear + 9,
        baseYear + 10 to baseYear + 14,
        baseYear + 15 to baseYear + 19
    )

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
                                    // Let's pass the start year of the range
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
    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

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

                    // Empty box for alignment
                    Box(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(months.indices.toList()) { index ->
                        val isCurrentMonth = index == currentMonth && selectedYear == currentYear
                        Text(
                            text = months[index],
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