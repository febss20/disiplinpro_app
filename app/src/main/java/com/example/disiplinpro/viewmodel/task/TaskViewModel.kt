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
import com.example.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.example.disiplinpro.data.repository.FirestoreRepository
import com.example.disiplinpro.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private var appContext: Context? = null

    private val pendingReschedules = mutableMapOf<String, Task>()

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

    fun provideAppContext(context: Context) {
        this.appContext = context.applicationContext
        if (pendingReschedules.isNotEmpty()) {
            Log.d("TaskViewModel", "Scheduling ${pendingReschedules.size} pending notifications")
            val iterator = pendingReschedules.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                scheduleNotification(appContext!!, entry.value)
                iterator.remove()
            }
        }
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
        this.appContext = context.applicationContext
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
                return@launch
            }

            val task = _tasks.value.find { it.id == taskId }
            if (task != null) {
                if (isCompleted) {
                    Log.d("TaskViewModel", "Task completed, cancelling notifications for task: ${task.judulTugas}")
                    WorkManager.getInstance().cancelUniqueWork("${NotificationWorker.WORK_NAME_PREFIX}$taskId")
                    WorkManager.getInstance().cancelUniqueWork("${NotificationWorker.OVERDUE_WORK_NAME_PREFIX}$taskId")
                } else {
                    Log.d("TaskViewModel", "Task uncompleted, preparing to reschedule notification for task: ${task.judulTugas}")
                    if (appContext != null) {
                        scheduleNotification(appContext!!, task)
                    } else {
                        pendingReschedules[taskId] = task
                        Log.d("TaskViewModel", "No context available, added to pending reschedules. Total pending: ${pendingReschedules.size}")
                    }
                }
            }
        }
    }

    fun updateTask(context: Context, taskId: String, updatedTask: Task) {
        this.appContext = context.applicationContext
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
                WorkManager.getInstance().cancelUniqueWork("${NotificationWorker.OVERDUE_WORK_NAME_PREFIX}$taskId")
                pendingReschedules.remove(taskId)
                Log.d("TaskViewModel", "Task deleted and notifications cancelled: $taskId")
            }
        }
    }

    fun scheduleNotification(context: Context, task: Task) {
        if (task.isCompleted || (task.completed == true)) {
            Log.d("TaskViewModel", "Not scheduling notification for completed task: ${task.judulTugas}")
            return
        }

        val securityPrefs = SecurityPrivacyPreferences(context)
        val globalNotificationsEnabled = runBlocking { securityPrefs.allowNotificationsFlow.first() }

        if (!globalNotificationsEnabled) {
            Log.d("TaskViewModel", "Global notifications disabled, not scheduling for task: ${task.judulTugas}")
            return
        }

        val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val notificationEnabled = prefs.getBoolean("taskNotificationEnabled", false)
        if (!notificationEnabled) {
            Log.d("TaskViewModel", "Task notifications disabled for task: ${task.judulTugas}")
            return
        }

        val deadlineTime = task.waktu.toDate().time
        val currentTime = System.currentTimeMillis()

        // 1. Jadwalkan notifikasi sebelum deadline (pengingat)
        val timeBefore = prefs.getString("taskTimeBefore", "1 Jam sebelum") ?: "1 Jam sebelum"
        val delay = when (timeBefore) {
            "10 Menit sebelum" -> 10 * 60 * 1000L
            "30 Menit sebelum" -> 30 * 60 * 1000L
            "1 Jam sebelum" -> 60 * 60 * 1000L
            "1 Hari sebelum" -> 24 * 60 * 60 * 1000L
            else -> 0L
        }

        val reminderTime = deadlineTime - delay

        if (reminderTime > currentTime) {
            val reminderData = workDataOf(
                "title" to "Pengingat Tugas: ${task.judulTugas}",
                "message" to "Tugas untuk ${task.matkul} jatuh tempo pada ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(task.waktu.toDate())}",
                "taskId" to task.id
            )

            val reminderWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(reminderTime - currentTime, TimeUnit.MILLISECONDS)
                .setInputData(reminderData)
                .addTag("notification_tag")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${NotificationWorker.WORK_NAME_PREFIX}${task.id}",
                    ExistingWorkPolicy.REPLACE,
                    reminderWorkRequest
                )
            Log.d("TaskViewModel", "Reminder notification for task ${task.judulTugas} at $reminderTime")
        } else {
            Log.w("TaskViewModel", "Reminder time for task ${task.judulTugas} has passed: $reminderTime")
        }

        // 2. Jadwalkan notifikasi keterlambatan pada waktu deadline
        if (deadlineTime > currentTime) {
            val overdueData = workDataOf(
                "title" to "Pengingat Tugas: ${task.judulTugas}",
                "message" to "Tugas untuk mata kuliah ${task.matkul} terlambat diselesaikan. Jatuh tempo pada: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(task.waktu.toDate())}",
                "taskId" to task.id,
                "isOverdue" to true
            )

            val overdueWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(deadlineTime - currentTime, TimeUnit.MILLISECONDS)
                .setInputData(overdueData)
                .addTag("overdue_notification_tag")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${NotificationWorker.OVERDUE_WORK_NAME_PREFIX}${task.id}",
                    ExistingWorkPolicy.REPLACE,
                    overdueWorkRequest
                )
            Log.d("TaskViewModel", "Overdue notification for task ${task.judulTugas} scheduled at $deadlineTime")
        } else {
            Log.w("TaskViewModel", "Deadline already passed for task ${task.judulTugas}: $deadlineTime")
        }
    }
}