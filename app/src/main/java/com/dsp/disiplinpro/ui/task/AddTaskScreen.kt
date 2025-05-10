package com.dsp.disiplinpro.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.WatchLater
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
import com.dsp.disiplinpro.data.model.Task
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.*
import com.dsp.disiplinpro.viewmodel.task.TaskViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val secondaryTextColor = if (isDarkMode) DarkTextGrey else Color.Gray
    val buttonColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
    val cancelColor = if (isDarkMode) Color(0xFFFF8A65) else Color(0xFFFF5722)
    val fieldBackgroundColor = if (isDarkMode) DarkCardBackground else Color(0x1A2196F3)
    val fieldBorderColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFFFFFFF)

    var judulTugas by remember { mutableStateOf("") }
    var matkul by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }
    var waktu by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
                "Tambah Tugas",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Batal",
                color = cancelColor,
                fontSize = 14.sp,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }

        Text(
            "Judul Tugas",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 22.dp, start = 31.dp)
        )
        BasicTextField(
            value = judulTugas,
            onValueChange = { judulTugas = it },
            textStyle = TextStyle(color = textColor, fontSize = 18.sp),
            modifier = Modifier
                .padding(top = 8.dp, start = 25.dp, end = 25.dp)
                .border(1.dp, fieldBorderColor, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(fieldBackgroundColor)
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (judulTugas.isEmpty()) {
                    Text("Masukkan Judul Tugas", color = secondaryTextColor, fontSize = 18.sp)
                }
                innerTextField()
            }
        )

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
            "Atur Tanggal dan Waktu Deadline",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 22.dp, start = 31.dp)
        )

        // Tanggal Deadline
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 10.dp, start = 31.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = "Tanggal",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF7DAFCB)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tanggal Deadline", color = textColor, fontSize = 18.sp)
        }
        Box(
            modifier = Modifier
                .padding(top = 8.dp, start = 25.dp, end = 230.dp)
                .border(1.dp, fieldBorderColor, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(fieldBackgroundColor)
                .clickable { showDatePicker = true }
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (tanggal.isEmpty()) "Pilih Tanggal" else tanggal,
                color = if (tanggal.isEmpty()) secondaryTextColor else textColor,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Waktu Deadline
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, start = 31.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.WatchLater,
                contentDescription = "Waktu",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFFFF8A65)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Waktu Deadline", color = textColor, fontSize = 18.sp)
        }
        Box(
            modifier = Modifier
                .padding(top = 8.dp, start = 25.dp, end = 250.dp)
                .border(1.dp, fieldBorderColor, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(fieldBackgroundColor)
                .clickable { showTimePicker = true }
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (waktu.isEmpty()) "Pilih Waktu" else waktu,
                color = if (waktu.isEmpty()) secondaryTextColor else textColor,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // DatePicker untuk Tanggal Deadline
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate.timeInMillis = millis
                            tanggal = dateFormat.format(selectedDate.time)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // TimePicker untuk Waktu Deadline
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = selectedTime.get(Calendar.HOUR_OF_DAY),
                initialMinute = selectedTime.get(Calendar.MINUTE),
                is24Hour = true
            )
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                onConfirm = {
                    selectedTime.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    selectedTime.set(Calendar.MINUTE, timePickerState.minute)
                    waktu = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }

        // Tombol Simpan
        Button(
            onClick = {
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val deadline = Timestamp(dateTimeFormat.parse("$tanggal $waktu") ?: Date())
                val task = Task(
                    judulTugas = judulTugas,
                    matkul = matkul,
                    tanggal = deadline,
                    waktu = deadline
                )
                viewModel.addTask(context, task)
                navController.popBackStack()
            },
            enabled = judulTugas.isNotBlank() && matkul.isNotBlank() && tanggal.isNotBlank() && waktu.isNotBlank(),
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