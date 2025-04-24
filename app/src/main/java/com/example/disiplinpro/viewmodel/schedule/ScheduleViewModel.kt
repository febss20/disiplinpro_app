package com.example.disiplinpro.viewmodel.schedule

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.data.repository.FirestoreRepository
import com.example.disiplinpro.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.disiplinpro.viewmodel.notification.NotificationViewModel

class ScheduleViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules
    private val notificationViewModel = NotificationViewModel()

    init {
        listenToSchedules()
    }

    private fun listenToSchedules() {
        repository.listenToSchedules(
            onDataChanged = { scheduleList ->
                Log.d("ScheduleViewModel", "Schedules fetched: ${scheduleList.size}")
                _schedules.value = scheduleList
            },
            onError = { error ->
                Log.e("ScheduleViewModel", "Error fetching schedules: ${error.message}")
            }
        )
    }

    fun addSchedule(context: Context, schedule: Schedule) {
        viewModelScope.launch {
            val success = repository.addSchedule(schedule)
            if (success) {
                Log.d("ScheduleViewModel", "Schedule added: ${schedule.matkul}")
                notificationViewModel.scheduleNotification(context, schedule)
            }
        }
    }

    fun updateSchedule(context: Context, scheduleId: String, updatedSchedule: Schedule) {
        viewModelScope.launch {
            val success = repository.updateSchedule(scheduleId, updatedSchedule)
            if (success) {
                Log.d("ScheduleViewModel", "Schedule updated: ${updatedSchedule.matkul}")
                notificationViewModel.scheduleNotification(context, updatedSchedule)
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            val success = repository.deleteSchedule(scheduleId)
            if (success) {
                Log.d("ScheduleViewModel", "Schedule deleted: $scheduleId")
                WorkManager.getInstance().cancelUniqueWork("${NotificationWorker.WORK_NAME_PREFIX}$scheduleId")
            }
        }
    }
}