package com.example.disiplinpro.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.viewmodel.schedule.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSchedulesScreen(navController: NavController, viewModel: ScheduleViewModel = viewModel()) {
    val schedules by viewModel.schedules.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Semua Jadwal") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(schedules) { schedule ->
                ScheduleItem(schedule)
            }
        }
    }
}

@Composable
fun ScheduleItem(schedule: Schedule) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Mata Kuliah: ${schedule.matkul}")
            Text(text = "Hari: ${schedule.hari}")
            Text(
                text = "Waktu: ${
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())
                } - ${
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuSelesai.toDate())
                }"
            )
            Text(text = "Ruangan: ${schedule.ruangan}")
        }
    }
}