package com.example.disiplinpro.ui.schedule

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.ui.theme.DisiplinproTheme
import com.example.disiplinpro.viewmodel.schedule.ScheduleViewModel
import com.google.firebase.Timestamp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(navController: NavController, viewModel: ScheduleViewModel = viewModel()) {
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

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
    ) {
        // Header: Tambah Jadwal dan Batal
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 55.dp, bottom = 12.dp, start = 31.dp, end = 31.dp)
        ) {
            Text(
                "Tambah Jadwal",
                color = Color(0xFF333333),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Batal",
                color = Color(0xFFFF5722),
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { navController.popBackStack() }
            )
        }

        // Bagian Dropdown Hari
        Box(
            modifier = Modifier
                .padding(start = 25.dp, end = 25.dp)
                .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x1A2196F3))
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Hari",
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 16.dp) // Jarak antara teks dan dropdown
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .weight(1f) // Mengisi sisa ruang
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                ) {
                    TextField(
                        value = hari,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = TextStyle(color = Color(0xFF333333), fontSize = 18.sp),
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
                        modifier = Modifier.background(Color(0x1A2196F3))
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day, color = Color(0xFF333333), fontSize = 16.sp) },
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

        // Mata Kuliah
        Text(
            "Mata Kuliah",
            color = Color(0xFF333333),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 22.dp, start = 31.dp)
        )
        BasicTextField(
            value = matkul,
            onValueChange = { matkul = it },
            textStyle = TextStyle(color = Color(0xFF333333), fontSize = 18.sp),
            modifier = Modifier
                .padding(top = 8.dp, start = 25.dp, end = 25.dp)
                .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x1A2196F3))
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (matkul.isEmpty()) {
                    Text("Masukkan Mata Kuliah", color = Color.Gray, fontSize = 18.sp)
                }
                innerTextField()
            }
        )

        // Atur Ruangan dan Waktu
        Text(
            "Atur Ruangan dan Waktu",
            color = Color(0xFF333333),
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
            Text("Ruangan", color = Color(0xFF333333), fontSize = 18.sp)
        }
        BasicTextField(
            value = ruangan,
            onValueChange = { ruangan = it },
            textStyle = TextStyle(color = Color(0xFF333333), fontSize = 18.sp),
            modifier = Modifier
                .padding(top = 8.dp, start = 25.dp, end = 25.dp)
                .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x1A2196F3))
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (ruangan.isEmpty()) {
                    Text("Masukkan Ruangan", color = Color.Gray, fontSize = 18.sp)
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
            Text("Waktu", color = Color(0xFF333333), fontSize = 18.sp)
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, start = 25.dp, end = 25.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1A2196F3))
                        .clickable { showStartTimePicker = true }
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = if (waktuMulai.isEmpty()) "Mulai" else waktuMulai,
                        color = if (waktuMulai.isEmpty()) Color.Gray else Color(0xFF333333),
                        fontSize = 18.sp,
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1A2196F3))
                        .clickable { showEndTimePicker = true }
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = if (waktuSelesai.isEmpty()) "Selesai" else waktuSelesai,
                        color = if (waktuSelesai.isEmpty()) Color.Gray else Color(0xFF333333),
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

        Button(
            onClick = {
                val startTimeTimestamp = Timestamp(timeFormat.parse(waktuMulai) ?: Date())
                val endTimeTimestamp = Timestamp(timeFormat.parse(waktuSelesai) ?: Date())
                val schedule = Schedule(
                    matkul = matkul,
                    hari = hari,
                    waktuMulai = startTimeTimestamp,
                    waktuSelesai = endTimeTimestamp,
                    ruangan = ruangan
                )
                viewModel.addSchedule(schedule)
                navController.popBackStack()
            },
            enabled = matkul.isNotBlank() && hari.isNotBlank() && waktuMulai.isNotBlank() && waktuSelesai.isNotBlank() && ruangan.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 31.dp, end = 31.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DAFCB)),
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

@Preview(showBackground = true)
@Composable
fun PreviewAddScheduleScreen() {
    DisiplinproTheme {
        AddScheduleScreen(rememberNavController())
    }
}