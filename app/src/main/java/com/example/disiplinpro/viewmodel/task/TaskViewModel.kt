package com.example.disiplinpro.viewmodel.task

import android.content.Context
import android.util.Log
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
        listenToTasks()
    }

    private fun listenToTasks() {
        repository.listenToTasks(
            onDataChanged = { taskList ->
                _tasks.value = taskList
                Log.d("TaskViewModel", "Tasks updated: ${taskList.size}")
            },
            onError = { error ->
                Log.e("TaskViewModel", "Error fetching tasks: ${error.message}")
            }
        )
    }

    fun fetchTasks() {
        viewModelScope.launch {
            try {
                val newTasks = repository.getTasks()
                _tasks.value = newTasks
                Log.d("TaskViewModel", "Manually fetched ${newTasks.size} tasks")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error manually fetching tasks: ${e.message}")
            }
        }
    }

    fun addTask(context: Context, task: Task) {
        viewModelScope.launch {
            val success = repository.addTask(task)
            if (success) {
                scheduleNotification(context, task)
            }
        }
    }

    fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val success = repository.updateTaskCompletion(taskId, isCompleted)
            if (!success) {
                Log.e("TaskViewModel", "Failed to update task completion for taskId: $taskId")
            }
        }
    }

    fun updateTask(context: Context, taskId: String, updatedTask: Task) {
        viewModelScope.launch {
            val success = repository.updateTask(taskId, updatedTask)
            if (success) {
                scheduleNotification(context, updatedTask)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val success = repository.deleteTask(taskId)
            if (success) {
                WorkManager.getInstance().cancelUniqueWork("${NotificationWorker.WORK_NAME_PREFIX}$taskId")
                Log.d("TaskViewModel", "Task deleted and notification cancelled: $taskId")
            }
        }
    }

    fun scheduleNotification(context: Context, task: Task) {
        val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val notificationEnabled = prefs.getBoolean("taskNotificationEnabled", false)
        if (!notificationEnabled) {
            Log.d("TaskViewModel", "Task notifications disabled for task: ${task.judulTugas}")
            return
        }

        val timeBefore = prefs.getString("taskTimeBefore", "1 Jam sebelum") ?: "1 Jam sebelum"
        val delay = when (timeBefore) {
            "10 Menit sebelum" -> 10 * 60 * 1000L
            "30 Menit sebelum" -> 30 * 60 * 1000L
            "1 Jam sebelum" -> 60 * 60 * 1000L
            "1 Hari sebelum" -> 24 * 60 * 60 * 1000L
            else -> 0L
        }

        val triggerTime = task.waktu.toDate().time - delay
        val currentTime = System.currentTimeMillis()
        if (triggerTime <= currentTime) {
            Log.w("TaskViewModel", "Trigger time for task ${task.judulTugas} has passed: $triggerTime")
            return
        }

        val data = workDataOf(
            "title" to "Pengingat Tugas: ${task.judulTugas}",
            "message" to "Tugas untuk ${task.matkul} jatuh tempo pada ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(task.waktu.toDate())}",
            "taskId" to task.id
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(triggerTime - currentTime, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("notification_tag")
            .build()

        WorkManager.getInstance()
            .enqueueUniqueWork(
                "${NotificationWorker.WORK_NAME_PREFIX}${task.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        Log.d("TaskViewModel", "Task notification for task ${task.judulTugas} at $triggerTime")
    }
}