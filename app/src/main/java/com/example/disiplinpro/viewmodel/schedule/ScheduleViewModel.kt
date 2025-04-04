package com.example.disiplinpro.viewmodel.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    init {
        listenToSchedules()
    }

    private fun listenToSchedules() {
        repository.listenToSchedules(
            onDataChanged = { scheduleList ->
                println("Schedules fetched from Firestore: $scheduleList")
                _schedules.value = scheduleList
            },
            onError = { error ->
                println("Error fetching schedules: ${error.message}")
            }
        )
    }

    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val success = repository.addSchedule(schedule)
            if (success) {
                println("Schedule added successfully")
            }
        }
    }

    fun updateSchedule(scheduleId: String, updatedSchedule: Schedule) {
        viewModelScope.launch {
            val success = repository.updateSchedule(scheduleId, updatedSchedule)
            if (success) {
                println("Schedule updated successfully")
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            val success = repository.deleteSchedule(scheduleId)
            if (success) {
                println("Schedule deleted successfully")
            }
        }
    }
}