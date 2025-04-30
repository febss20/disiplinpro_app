package com.dsp.disiplinpro.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dsp.disiplinpro.data.model.Schedule
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.*
import com.dsp.disiplinpro.viewmodel.schedule.ScheduleViewModel
import com.google.firebase.Timestamp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = viewModel()
) {
    val context = LocalContext.current

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val secondaryTextColor = if (isDarkMode) DarkTextGrey else Color.Gray
    val accentColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
    val buttonColor = accentColor
    val cancelColor = if (isDarkMode) Color(0xFFFF8A65) else Color(0xFFFF5722)
    val fieldBackgroundColor = if (isDarkMode) DarkCardBackground else Color(0x1A2196F3)
    val fieldBorderColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFFFFFFF)
    val infoTextColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)

    var matkul by remember { mutableStateOf("") }
    var hari by remember { mutableStateOf("") }
    var waktuMulai by remember { mutableStateOf("") }
    var waktuSelesai by remember { mutableStateOf("") }
    var ruangan by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(Calendar.getInstance()) }
    var endTime by remember { mutableStateOf(Calendar.getInstance()) }
    var errorMessage by remember { mutableStateOf("") }

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 50.dp, bottom = 12.dp, start = 31.dp, end = 31.dp)
        ) {
            Text(
                "Tambah Jadwal",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Batal",
                color = cancelColor,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { navController.popBackStack() }
            )
        }

        // Informasi bahwa jadwal akan berulang
        if (hari.isNotBlank()) {
            Text(
                "Jadwal ini akan berulang setiap $hari",
                color = infoTextColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 31.dp, bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 25.dp, end = 25.dp)
                .border(1.dp, fieldBorderColor, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(fieldBackgroundColor)
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Hari",
                    color = textColor,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, fieldBorderColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                ) {
                    TextField(
                        value = hari,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = TextStyle(color = textColor, fontSize = 18.sp),
                        trailingIcon = {
                            CoilImage(
                                imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/q3j41g0n.png" },
                                imageOptions = ImageOptions(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .background(Color.Transparent)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(if (isDarkMode) DarkCardBackground else Color(0x1A2196F3))
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day, color = textColor, fontSize = 16.sp) },
                                onClick = {
                                    hari = day
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Text(
            "Mata Kuliah",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 22.dp, start = 31.dp)
        )
        BasicTextField(
            value = matkul,
            onValueChange = { matkul = it },
            textStyle = TextStyle(color = textColor, fontSize = 18.sp),
            modifier = Modifier
                .padding(top = 8.dp, start = 25.dp, end = 25.dp)
                .border(1.dp, fieldBorderColor, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(fieldBackgroundColor)
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (matkul.isEmpty()) {
                    Text("Masukkan Mata Kuliah", color = secondaryTextColor, fontSize = 18.sp)
                }
                innerTextField()
            }
        )

        Text(
            "Atur Ruangan dan Waktu",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 22.dp, start = 31.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, start = 31.dp)
        ) {
            CoilImage(
                imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/5wgv1t8r.png" },
                imageOptions = ImageOptions(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                modifier = Modifier.width(35.dp).padding(end = 13.dp)
            )
            Text("Ruangan", color = textColor, fontSize = 18.sp)
        }
        BasicTextField(
            value = ruangan,
            onValueChange = { ruangan = it },
            textStyle = TextStyle(color = textColor, fontSize = 18.sp),
            modifier = Modifier
                .padding(top = 8.dp, start = 25.dp, end = 25.dp)
                .border(1.dp, fieldBorderColor, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(fieldBackgroundColor)
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (ruangan.isEmpty()) {
                    Text("Masukkan Ruangan", color = secondaryTextColor, fontSize = 18.sp)
                }
                innerTextField()
            }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, start = 31.dp)
        ) {
            CoilImage(
                imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/640pibqq.png" },
                imageOptions = ImageOptions(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                modifier = Modifier.width(35.dp).padding(end = 12.dp)
            )
            Text("Waktu", color = textColor, fontSize = 18.sp)
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, start = 25.dp, end = 25.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, fieldBorderColor, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(fieldBackgroundColor)
                        .clickable { showStartTimePicker = true }
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = if (waktuMulai.isEmpty()) "Mulai" else waktuMulai,
                        color = if (waktuMulai.isEmpty()) secondaryTextColor else textColor,
                        fontSize = 18.sp,
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, fieldBorderColor, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(fieldBackgroundColor)
                        .clickable { showEndTimePicker = true }
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = if (waktuSelesai.isEmpty()) "Selesai" else waktuSelesai,
                        color = if (waktuSelesai.isEmpty()) secondaryTextColor else textColor,
                        fontSize = 18.sp,
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
            }

            if (showStartTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = startTime.get(Calendar.HOUR_OF_DAY),
                    initialMinute = startTime.get(Calendar.MINUTE),
                    is24Hour = true
                )
                TimePickerDialog(
                    onDismissRequest = { showStartTimePicker = false },
                    onConfirm = {
                        startTime.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        startTime.set(Calendar.MINUTE, timePickerState.minute)
                        waktuMulai = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showStartTimePicker = false
                    }
                ) {
                    TimePicker(state = timePickerState)
                }
            }

            if (showEndTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = endTime.get(Calendar.HOUR_OF_DAY),
                    initialMinute = endTime.get(Calendar.MINUTE),
                    is24Hour = true
                )
                TimePickerDialog(
                    onDismissRequest = { showEndTimePicker = false },
                    onConfirm = {
                        endTime.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        endTime.set(Calendar.MINUTE, timePickerState.minute)
                        waktuSelesai = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showEndTimePicker = false
                    }
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        }

        // Tampilkan pesan error jika ada
        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, start = 31.dp)
            )
        }

        Button(
            onClick = {
                val startHour = startTime.get(Calendar.HOUR_OF_DAY)
                val startMinute = startTime.get(Calendar.MINUTE)
                val endHour = endTime.get(Calendar.HOUR_OF_DAY)
                val endMinute = endTime.get(Calendar.MINUTE)

                if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
                    errorMessage = "Waktu selesai harus lebih besar dari waktu mulai"
                    return@Button
                }

                errorMessage = ""

                // Gunakan tanggal hari ini sebagai default, bukan 01/01/2025
                val currentDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, startTime.get(Calendar.MINUTE))
                }
                val startTimeTimestamp = Timestamp(currentDate.time)

                val endDate = currentDate.clone() as Calendar
                endDate.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY))
                endDate.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE))
                val endTimeTimestamp = Timestamp(endDate.time)

                val schedule = Schedule(
                    matkul = matkul,
                    hari = hari,
                    waktuMulai = startTimeTimestamp,
                    waktuSelesai = endTimeTimestamp,
                    ruangan = ruangan
                )
                viewModel.addSchedule(context, schedule)
                navController.popBackStack()
            },
            enabled = matkul.isNotBlank() && hari.isNotBlank() && waktuMulai.isNotBlank() && waktuSelesai.isNotBlank() && ruangan.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 25.dp, end = 25.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Simpan", color = Color.White, fontSize = 20.sp)
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        },
        text = { content() }
    )
}