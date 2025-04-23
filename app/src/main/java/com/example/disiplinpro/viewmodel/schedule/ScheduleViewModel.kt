package com.example.disiplinpro.viewmodel.schedule

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.example.disiplinpro.data.repository.FirestoreRepository
import com.example.disiplinpro.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
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

    fun scheduleNotification(context: Context, schedule: Schedule) {
        val securityPrefs = SecurityPrivacyPreferences(context)
        val globalNotificationsEnabled = runBlocking { securityPrefs.allowNotificationsFlow.first() }

        if (!globalNotificationsEnabled) {
            Log.d("ScheduleViewModel", "Global notifications disabled, not scheduling for schedule: ${schedule.matkul}")
            return
        }

        val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val notificationEnabled = prefs.getBoolean("scheduleNotificationEnabled", false)
        if (!notificationEnabled) {
            Log.d("ScheduleViewModel", "Schedule notifications disabled for: ${schedule.matkul}")
            return
        }

        val timeBefore =
            prefs.getString("scheduleTimeBefore", "30 Menit sebelum") ?: "30 Menit sebelum"
        val delayMinutes = when (timeBefore) {
            "10 Menit sebelum" -> 10
            "30 Menit sebelum" -> 30
            "1 Jam sebelum" -> 60
            "1 Hari sebelum" -> 24 * 60
            else -> 30
        }

        val dayOfWeek = when (schedule.hari) {
            "Senin" -> Calendar.MONDAY
            "Selasa" -> Calendar.TUESDAY
            "Rabu" -> Calendar.WEDNESDAY
            "Kamis" -> Calendar.THURSDAY
            "Jumat" -> Calendar.FRIDAY
            "Sabtu" -> Calendar.SATURDAY
            "Minggu" -> Calendar.SUNDAY
            else -> {
                Log.e("ScheduleViewModel", "Invalid day: ${schedule.hari}")
                return
            }
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, schedule.waktuMulai.toDate().hours)
        calendar.set(Calendar.MINUTE, schedule.waktuMulai.toDate().minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MINUTE, -delayMinutes)

        val data = workDataOf(
            "title" to "Pengingat Jadwal: ${schedule.matkul}",
            "message" to "Jadwal di ${schedule.ruangan} dimulai pukul ${
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(schedule.waktuMulai.toDate())
            }",
            "scheduleId" to schedule.id
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(calendar), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("notification_tag")
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "${NotificationWorker.WORK_NAME_PREFIX}${schedule.id}",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        Log.d("ScheduleViewModel", "Scheduled periodic notification for ${schedule.matkul}")
    }

    private fun calculateInitialDelay(targetCalendar: Calendar): Long {
        val currentTime = System.currentTimeMillis()
        var triggerTime = targetCalendar.timeInMillis
        if (triggerTime <= currentTime) {
            targetCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            triggerTime = targetCalendar.timeInMillis
        }
        return triggerTime - currentTime
    }
}