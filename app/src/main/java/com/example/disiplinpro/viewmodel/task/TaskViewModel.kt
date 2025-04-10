package com.example.disiplinpro.viewmodel.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.data.repository.FirestoreRepository
import com.example.disiplinpro.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        viewModelScope.launch {
            val newTasks = repository.getTasks()
            _tasks.value = newTasks
            println("Fetched ${newTasks.size} tasks")
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            val success = repository.addTask(task)
            if (success) {
                fetchTasks()
                scheduleNotification(task)
            }
        }
    }

    fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val success = repository.updateTaskCompletion(taskId, isCompleted)
            if (success) {
                fetchTasks()
                println("Tasks refreshed after update")
            } else {
                println("Failed to update task completion for taskId: $taskId")
            }
        }
    }

    fun updateTask(taskId: String, updatedTask: Task) {
        viewModelScope.launch {
            val success = repository.updateTask(taskId, updatedTask)
            if (success) {
                fetchTasks()
                scheduleNotification(updatedTask)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val success = repository.deleteTask(taskId)
            if (success) {
                fetchTasks()
                WorkManager.getInstance().cancelUniqueWork("${NotificationWorker.WORK_NAME_PREFIX}$taskId")
            }
        }
    }

    private fun scheduleNotification(task: Task) {
        val notificationEnabled = true // Ganti dengan logika dari NotificationScreen
        if (!notificationEnabled) return

        val timeBefore = "1 jam sebelum" // Ganti dengan nilai dari NotificationScreen
        val delay = when (timeBefore) {
            "10 menit sebelum" -> 10 * 60 * 1000L
            "30 menit sebelum" -> 30 * 60 * 1000L
            "1 jam sebelum" -> 60 * 60 * 1000L
            "1 hari sebelum" -> 24 * 60 * 60 * 1000L
            else -> 0L
        }

        val triggerTime = task.waktu.toDate().time - delay
        val currentTime = System.currentTimeMillis()
        if (triggerTime <= currentTime) return

        val data = workDataOf(
            "title" to "Pengingat Tugas: ${task.judulTugas}",
            "message" to "Tugas untuk ${task.matkul} jatuh tempo pada ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(task.waktu.toDate())}"
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(triggerTime - currentTime, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance()
            .enqueueUniqueWork(
                "${NotificationWorker.WORK_NAME_PREFIX}${task.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
}