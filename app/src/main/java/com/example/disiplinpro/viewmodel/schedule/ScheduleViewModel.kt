package com.example.disiplinpro.viewmodel.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.data.repository.FirestoreRepository
import com.example.disiplinpro.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
                scheduleNotification(schedule)
            }
        }
    }

    fun updateSchedule(scheduleId: String, updatedSchedule: Schedule) {
        viewModelScope.launch {
            val success = repository.updateSchedule(scheduleId, updatedSchedule)
            if (success) {
                println("Schedule updated successfully")
                scheduleNotification(updatedSchedule)
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            val success = repository.deleteSchedule(scheduleId)
            if (success) {
                println("Schedule deleted successfully")
                WorkManager.getInstance().cancelUniqueWork("${NotificationWorker.WORK_NAME_PREFIX}$scheduleId")
            }
        }
    }

    private fun scheduleNotification(schedule: Schedule) {
        val notificationEnabled = true // Ganti dengan logika dari NotificationScreen
        if (!notificationEnabled) return

        val timeBefore = "30 menit sebelum" // Ganti dengan nilai dari NotificationScreen
        val delay = when (timeBefore) {
            "10 menit sebelum" -> 10 * 60 * 1000L
            "30 menit sebelum" -> 30 * 60 * 1000L
            "1 jam sebelum" -> 60 * 60 * 1000L
            "1 hari sebelum" -> 24 * 60 * 60 * 1000L
            else -> 0L
        }

        val startTime = schedule.waktuMulai.toDate()
        println("Waktu Mulai Jadwal: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(startTime)}")

        val triggerTime = startTime.time - delay
        val currentTime = System.currentTimeMillis()
        println("Current Time: $currentTime, Trigger Time: $triggerTime, Delay: ${triggerTime - currentTime}")

        if (triggerTime <= currentTime) {
            println("Waktu pemicu sudah lewat, notifikasi tidak dijadwalkan")
            return
        }

        val data = workDataOf(
            "title" to "Pengingat Jadwal: ${schedule.matkul}",
            "message" to "Jadwal di ${schedule.ruangan} akan dimulai pada ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())}"
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(triggerTime - currentTime, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance()
            .enqueueUniqueWork(
                "${NotificationWorker.WORK_NAME_PREFIX}${schedule.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
}