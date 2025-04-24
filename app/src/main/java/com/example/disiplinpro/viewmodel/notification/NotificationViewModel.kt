package com.example.disiplinpro.viewmodel.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.disiplinpro.data.model.Notification
import com.example.disiplinpro.data.model.NotificationType
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.example.disiplinpro.data.repository.NotificationRepository
import com.example.disiplinpro.receiver.AlarmReceiver
import com.example.disiplinpro.util.AlarmHelper
import com.example.disiplinpro.worker.NotificationWorker
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()

    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error = _error

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _currentFilter = MutableStateFlow<NotificationType?>(null)
    val currentFilter: StateFlow<NotificationType?> = _currentFilter.asStateFlow()

    init {
        loadNotifications()
    }

    /**
     * Load semua notifikasi
     */
    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val notificationList = repository.getAllNotifications()
                _notifications.value = notificationList

                updateUnreadCount()
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error loading notifications: ${e.message}")
                _error.value = "Gagal memuat notifikasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update jumlah notifikasi yang belum dibaca
     */
    private fun updateUnreadCount() {
        val unreadNotifications = _notifications.value.count { !it.isRead }
        _unreadCount.value = unreadNotifications
        Log.d("NotificationVM", "Unread notifications count: $unreadNotifications")
    }

    /**
     * Filter notifikasi berdasarkan tipe
     */
    fun setFilter(type: NotificationType?) {
        _currentFilter.value = type
    }

    /**
     * Tandai notifikasi sebagai sudah dibaca
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val success = repository.markNotificationAsRead(notificationId)
            if (success) {
                val currentList = _notifications.value
                val updatedList = currentList.map {
                    if (it.id == notificationId) it.copy(isRead = true) else it
                }
                _notifications.value = updatedList

                updateUnreadCount()
            }
        }
    }

    /**
     * Hapus notifikasi
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            val success = repository.deleteNotification(notificationId)
            if (success) {
                val currentList = _notifications.value
                val updatedList = currentList.filter { it.id != notificationId }
                _notifications.value = updatedList

                updateUnreadCount()
            }
        }
    }

    /**
     * Hapus semua notifikasi yang sudah dibaca
     */
    fun clearReadNotifications() {
        viewModelScope.launch {
            val success = repository.clearReadNotifications()
            if (success) {
                val currentList = _notifications.value
                val updatedList = currentList.filter { !it.isRead }
                _notifications.value = updatedList
            }
        }
    }

    /**
     * Tandai semua notifikasi sebagai sudah dibaca
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            val unreadNotifications = _notifications.value.filter { !it.isRead }
            if (unreadNotifications.isEmpty()) return@launch

            val success = repository.markAllNotificationsAsRead(unreadNotifications.map { it.id })
            if (success) {
                val currentList = _notifications.value
                val updatedList = currentList.map { it.copy(isRead = true) }
                _notifications.value = updatedList

                _unreadCount.value = 0
            }
        }
    }

    /**
     * Format timestamp untuk ditampilkan
     */
    fun formatTimestamp(date: Date): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = date }
        val diff = now.timeInMillis - then.timeInMillis
        val days = diff / (24 * 60 * 60 * 1000)

        return when {
            days == 0L -> {
                val hours = diff / (60 * 60 * 1000)
                if (hours == 0L) {
                    val minutes = diff / (60 * 1000)
                    if (minutes == 0L) "Baru saja" else "$minutes menit lalu"
                } else {
                    "$hours jam lalu"
                }
            }
            days == 1L -> "Kemarin"
            days < 7L -> "$days hari lalu"
            else -> SimpleDateFormat("dd MMM yyyy", Locale("id")).format(date)
        }
    }

    /**
     * Jadwalkan notifikasi untuk tugas
     */
    fun scheduleNotification(context: Context, task: Task) {
        if (task.isCompleted || (task.completed == true)) {
            Log.d("NotificationViewModel", "Not scheduling notification for completed task: ${task.judulTugas}")
            return
        }

        val securityPrefs = SecurityPrivacyPreferences(context)
        val globalNotificationsEnabled = runBlocking { securityPrefs.allowNotificationsFlow.first() }

        if (!globalNotificationsEnabled) {
            Log.d("NotificationViewModel", "Global notifications disabled, not scheduling for task: ${task.judulTugas}")
            return
        }

        val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val notificationEnabled = prefs.getBoolean("taskNotificationEnabled", false)
        if (!notificationEnabled) {
            Log.d("NotificationViewModel", "Task notifications disabled for task: ${task.judulTugas}")
            return
        }

        val deadlineTime = task.waktu.toDate().time
        val currentTime = System.currentTimeMillis()

        // 1. Jadwalkan notifikasi sebelum deadline (pengingat)
        val timeBefore = prefs.getString("taskTimeBefore", "1 Jam sebelum") ?: "1 Jam sebelum"
        val delayMinutes = when (timeBefore) {
            "10 Menit sebelum" -> 10
            "30 Menit sebelum" -> 30
            "1 Jam sebelum" -> 60
            "3 Jam sebelum" -> 3 * 60
            "1 Hari sebelum" -> 24 * 60
            else -> 60
        }

        val reminderTime = deadlineTime - (delayMinutes * 60 * 1000)
        if (reminderTime > currentTime) {
            val data = workDataOf(
                "title" to "Pengingat Tugas: ${task.judulTugas}",
                "message" to "Tugas ${task.judulTugas} untuk mata kuliah ${task.matkul} jatuh tempo pada ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(task.waktu.toDate())}",
                "taskId" to task.id
            )

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(reminderTime - currentTime, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("notification_tag")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${NotificationWorker.WORK_NAME_PREFIX}${task.id}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            Log.d("NotificationViewModel", "Task reminder scheduled: ${task.judulTugas} at ${Date(reminderTime)}")
        } else {
            Log.w("NotificationViewModel", "Reminder time already passed for task ${task.judulTugas}")
        }

        // 2. Jadwalkan notifikasi pada waktu deadline (terlambat)
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
            Log.d("NotificationViewModel", "Overdue notification for task ${task.judulTugas} scheduled at $deadlineTime")
        } else {
            Log.w("NotificationViewModel", "Deadline already passed for task ${task.judulTugas}: $deadlineTime")
        }
    }

    /**
     * Jadwalkan notifikasi untuk jadwal kuliah
     */
    fun scheduleNotification(context: Context, schedule: Schedule) {
        val securityPrefs = SecurityPrivacyPreferences(context)
        val globalNotificationsEnabled = runBlocking { securityPrefs.allowNotificationsFlow.first() }

        if (!globalNotificationsEnabled) {
            Log.d("NotificationViewModel", "Global notifications disabled, not scheduling for schedule: ${schedule.matkul}")
            return
        }

        val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val notificationEnabled = prefs.getBoolean("scheduleNotificationEnabled", false)
        if (!notificationEnabled) {
            Log.d("NotificationViewModel", "Schedule notifications disabled for: ${schedule.matkul}")
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
                Log.e("NotificationViewModel", "Invalid day: ${schedule.hari}")
                return
            }
        }

        val today = Calendar.getInstance()
        val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)

        // 1. Jadwalkan untuk jadwal hari ini jika belum lewat
        if (todayDayOfWeek == dayOfWeek) {
            Log.d("NotificationViewModel", "Scheduling for today (${schedule.hari}): ${schedule.matkul}")
            scheduleForSpecificDay(context, schedule, today, delayMinutes)
        }

        // 2. Jadwalkan untuk jadwal di hari berikutnya
        val nextDay = Calendar.getInstance()
        if (todayDayOfWeek <= dayOfWeek) {
            nextDay.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        } else {
            nextDay.add(Calendar.WEEK_OF_YEAR, 1)
            nextDay.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        }

        if (todayDayOfWeek != dayOfWeek) {
            Log.d("NotificationViewModel", "Scheduling for next ${schedule.hari}: ${schedule.matkul}")
            scheduleForSpecificDay(context, schedule, nextDay, delayMinutes)
        }

        // 3. Jadwalkan WorkManager periodic untuk jangka panjang
        scheduleLongTermPeriodic(context, schedule, dayOfWeek, delayMinutes)

        // 4. Jadwalkan alarm mingguan berulang untuk beberapa minggu ke depan (keandalan tambahan)
        try {
            val alarmHelper = AlarmHelper(context)
            alarmHelper.scheduleRecurringWeeklyAlarm(
                schedule,
                dayOfWeek,
                schedule.waktuMulai.toDate().hours,
                schedule.waktuMulai.toDate().minutes,
                delayMinutes
            )
            Log.d("NotificationViewModel", "Recurring weekly alarms scheduled for ${schedule.matkul}")
        } catch (e: Exception) {
            Log.e("NotificationViewModel", "Failed to schedule recurring alarms: ${e.message}")
        }

        val scheduleInfo = mapOf(
            "id" to schedule.id,
            "matkul" to schedule.matkul,
            "ruangan" to schedule.ruangan,
            "dayOfWeek" to dayOfWeek,
            "hour" to schedule.waktuMulai.toDate().hours,
            "minute" to schedule.waktuMulai.toDate().minutes,
            "delayMinutes" to delayMinutes
        )
        saveScheduleInfo(context, schedule.id, scheduleInfo)
    }

    private fun scheduleForSpecificDay(context: Context, schedule: Schedule, targetDay: Calendar, delayMinutes: Int) {
        val scheduleTime = Calendar.getInstance()
        scheduleTime.timeInMillis = targetDay.timeInMillis
        scheduleTime.set(Calendar.HOUR_OF_DAY, schedule.waktuMulai.toDate().hours)
        scheduleTime.set(Calendar.MINUTE, schedule.waktuMulai.toDate().minutes)
        scheduleTime.set(Calendar.SECOND, 0)
        scheduleTime.set(Calendar.MILLISECOND, 0)

        val notificationTime = (scheduleTime.clone() as Calendar)
        notificationTime.add(Calendar.MINUTE, -delayMinutes)

        if (notificationTime.timeInMillis <= System.currentTimeMillis()) {
            Log.d("NotificationViewModel", "Notification time has passed for ${schedule.matkul} on ${targetDay.time}")
            return
        }

        val data = workDataOf(
            "title" to "Pengingat Jadwal: ${schedule.matkul}",
            "message" to "Jadwal di ${schedule.ruangan} dimulai pukul ${
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())
            }",
            "scheduleId" to schedule.id
        )

        val initialDelayMs = notificationTime.timeInMillis - System.currentTimeMillis()
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("notification_tag")
            .build()

        val workName = "${NotificationWorker.WORK_NAME_PREFIX}_${schedule.id}_${targetDay.get(Calendar.DAY_OF_YEAR)}"
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )

        Log.d("NotificationViewModel", "OneTime notification for ${schedule.matkul} scheduled at ${notificationTime.time}, delay: ${initialDelayMs}ms")

        try {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("scheduleId", schedule.id)
                putExtra("title", "Pengingat Jadwal: ${schedule.matkul}")
                putExtra("message", "Jadwal di ${schedule.ruangan} dimulai pukul ${
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())
                }")
                putExtra("isSchedule", true)
            }

            val uniqueId = ("schedule_${schedule.id}_${targetDay.get(Calendar.DAY_OF_YEAR)}").hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.timeInMillis,
                    pendingIntent
                )
            }

            Log.d("NotificationViewModel", "Backup alarm for ${schedule.matkul} scheduled at ${notificationTime.time}")
        } catch (e: Exception) {
            Log.e("NotificationViewModel", "Failed to schedule backup alarm: ${e.message}")
        }
    }

    private fun scheduleLongTermPeriodic(context: Context, schedule: Schedule, dayOfWeek: Int, delayMinutes: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, schedule.waktuMulai.toDate().hours)
        calendar.set(Calendar.MINUTE, schedule.waktuMulai.toDate().minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MINUTE, -delayMinutes)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val data = workDataOf(
            "title" to "Pengingat Jadwal: ${schedule.matkul}",
            "message" to "Jadwal di ${schedule.ruangan} dimulai pukul ${
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())
            }",
            "scheduleId" to schedule.id,
            "dayOfWeek" to dayOfWeek,
            "hour" to schedule.waktuMulai.toDate().hours,
            "minute" to schedule.waktuMulai.toDate().minutes,
            "delayMinutes" to delayMinutes
        )

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            7, TimeUnit.DAYS,
            5, TimeUnit.MINUTES
        )
            .setInitialDelay(calendar.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("notification_tag")
            .addTag("schedule_notification")
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "${NotificationWorker.WORK_NAME_PREFIX}${schedule.id}",
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )

        Log.d("NotificationViewModel", "Periodic notification for ${schedule.matkul} scheduled at ${calendar.time} (repeating weekly)")
    }

    private fun saveScheduleInfo(context: Context, scheduleId: String, scheduleInfo: Map<String, Any>) {
        val prefs = context.getSharedPreferences("ScheduledNotifications", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(scheduleInfo)
        prefs.edit().putString("schedule_$scheduleId", json).apply()
    }
}