package com.dsp.disiplinpro.ui.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsp.disiplinpro.data.model.Schedule
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import com.dsp.disiplinpro.R

@Composable
fun ScheduleItem(
    schedules: List<Schedule>,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val containerColor = if (isSelected) {
        if (isDarkMode) DarkPrimaryBlue.copy(alpha = 0.3f) else Color(0x802196F3)
    } else {
        if (isDarkMode) DarkCardBackground else Color(0x332196F3)
    }
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val dividerColor = if (isDarkMode) Color(0xFF444444) else Color(0xFF000000)
    val iconTint = if (isDarkMode) DarkTextLight else Color(0xFF333333)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp, bottom = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
    ) {
        Column(modifier = Modifier.padding(top = 14.dp, bottom = 22.dp)) {
            Text(
                text = schedules.first().hari,
                color = textColor,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 1.dp, bottom = 12.dp, start = 23.dp)
            )
            Spacer(modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .padding(start = 23.dp, end = 23.dp)
                .background(dividerColor))
            schedules.forEach { schedule ->
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp, start = 23.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Circle,
                            contentDescription = "Points",
                            modifier = Modifier.size(15.dp),
                            tint = iconTint
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = schedule.matkul,
                            color = textColor,
                            fontSize = 19.sp,
                            modifier = Modifier.padding(start = 5.dp),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 45.dp, top = 9.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DoorFront,
                            contentDescription = "Ruangan",
                            modifier = Modifier.size(24.dp),
                            tint = iconTint
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = schedule.ruangan,
                            color = textColor,
                            fontSize = 19.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 45.dp, top = 9.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WatchLater,
                            contentDescription = "Waktu",
                            modifier = Modifier.size(24.dp),
                            tint = iconTint
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())} - " +
                                    "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuSelesai.toDate())}",
                            color = textColor,
                            fontSize = 19.sp
                        )
                    }
                }
            }
        }
    }
}