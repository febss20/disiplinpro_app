package com.dsp.disiplinpro.viewmodel.task

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.dsp.disiplinpro.data.model.Task
import com.dsp.disiplinpro.data.repository.FirestoreRepository
import com.dsp.disiplinpro.viewmodel.notification.NotificationViewModel
import com.dsp.disiplinpro.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private var appContext: Context? = null
    private val notificationViewModel = NotificationViewModel()

    private val pendingReschedules = mutableMapOf<String, Task>()
    private val _isLoading = MutableStateFlow(false)

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
                notificationViewModel.scheduleNotification(appContext!!, entry.value)
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
                notificationViewModel.scheduleNotification(context, task)
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
                        notificationViewModel.scheduleNotification(appContext!!, task)
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
                notificationViewModel.scheduleNotification(context, updatedTask)
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

    suspend fun getTodayTasks() {
        _isLoading.value = true
        try {
            val todayTaskList = repository.getTodayTasks()
            _tasks.value = todayTaskList
            Log.d(TAG, "Fetched ${todayTaskList.size} tasks for today")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching today's tasks: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getRecentTasks(limit: Int = 5) {
        _isLoading.value = true
        try {
            val recentTaskList = repository.getRecentTasks(limit)
            _tasks.value = recentTaskList
            Log.d(TAG, "Fetched ${recentTaskList.size} recent tasks")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recent tasks: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
}