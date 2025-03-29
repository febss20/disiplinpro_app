package com.example.disiplinpro.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.viewmodel.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(navController: NavController, viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Semua Tugas") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(tasks) { task ->
                TaskItem(task, viewModel)
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, viewModel: TaskViewModel) {
    var isChecked by remember { mutableStateOf(task.isCompleted) }

    // Update local state ketika task berubah
    LaunchedEffect(task) {
        isChecked = task.isCompleted
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                    isChecked = checked
                    viewModel.updateTaskCompletion(task.id, checked)
                }
            )
            Column {
                Text(text = "Tugas: ${task.judulTugas}")
                Text(text = "Mata Kuliah: ${task.matkul}")
                Text(text = "Deadline: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(task.tanggal.toDate())}")
            }
        }
    }
}