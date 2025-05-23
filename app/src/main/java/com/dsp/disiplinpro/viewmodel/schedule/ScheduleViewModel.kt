package com.dsp.disiplinpro.viewmodel.schedule

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.dsp.disiplinpro.data.model.Schedule
import com.dsp.disiplinpro.data.repository.FirestoreRepository
import com.dsp.disiplinpro.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.dsp.disiplinpro.viewmodel.notification.NotificationViewModel

class ScheduleViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules
    private val notificationViewModel = NotificationViewModel()
    private val _isLoading = MutableStateFlow(false)
    private val _allSchedules = MutableStateFlow<List<Schedule>>(emptyList())

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

    fun fetchSchedules() {
        viewModelScope.launch {
            try {
                val allSchedules = repository.getSchedules()
                _allSchedules.value = allSchedules
                Log.d("ScheduleViewModel", "Manually fetched all ${allSchedules.size} schedules")
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error manually fetching all schedules: ${e.message}")
            }
        }
    }

    suspend fun getAllSchedules(): List<Schedule> {
        return try {
            val allSchedules = repository.getSchedules()
            Log.d("ScheduleViewModel", "Retrieved all ${allSchedules.size} schedules")
            allSchedules
        } catch (e: Exception) {
            Log.e("ScheduleViewModel", "Error retrieving all schedules: ${e.message}")
            emptyList()
        }
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

    suspend fun getSchedulesByDay(day: String) {
        _isLoading.value = true
        try {
            val daySchedules = repository.getSchedulesByDay(day)
            _schedules.value = daySchedules
            Log.d("ScheduleViewModel", "Fetched ${daySchedules.size} schedules for day: $day")
        } catch (e: Exception) {
            Log.e("ScheduleViewModel", "Error fetching schedules for day $day: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
}