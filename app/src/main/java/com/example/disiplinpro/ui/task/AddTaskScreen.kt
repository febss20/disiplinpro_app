package com.example.disiplinpro.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.viewmodel.task.TaskViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController, viewModel: TaskViewModel = viewModel()) {
    var judulTugas by remember { mutableStateOf("") }
    var matkul by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }
    var waktu by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Tambah Tugas", style = MaterialTheme.typography.titleLarge)

        TextField(
            value = judulTugas,
            onValueChange = { judulTugas = it },
            label = { Text("Judul Tugas") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = matkul,
            onValueChange = { matkul = it },
            label = { Text("Mata Kuliah") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = tanggal,
            onValueChange = { tanggal = it },
            label = { Text("Tanggal Deadline (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = waktu,
            onValueChange = { waktu = it },
            label = { Text("Waktu Deadline (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

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
                viewModel.addTask(task)
                navController.popBackStack()
            },
            enabled = judulTugas.isNotBlank() && matkul.isNotBlank() && tanggal.isNotBlank() && waktu.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan")
        }
    }
}