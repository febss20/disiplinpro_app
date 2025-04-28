package com.dsp.disiplinpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsp.disiplinpro.data.model.Schedule
import com.dsp.disiplinpro.ui.theme.DarkCardLight
import com.dsp.disiplinpro.ui.theme.DarkTextLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.forEach

@Composable
fun ScheduleDescription(
    schedules: List<Schedule>,
    selectedDate: Calendar,
    timeFormat: SimpleDateFormat,
    isDarkMode: Boolean = false,
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
                    .background(if (isDarkMode) DarkCardLight else Color(0x1A2196F3))
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
                    color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    schedule.ruangan,
                    color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    schedule.matkul,
                    color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}