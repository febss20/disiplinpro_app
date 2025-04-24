package com.example.disiplinpro.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.disiplinpro.service.NotificationService
import com.example.disiplinpro.util.AlarmHelper
import com.example.disiplinpro.data.repository.FirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * BroadcastReceiver untuk menerima alarm dari AlarmManager
 * dan menampilkan notifikasi saat waktunya tiba, termasuk menjadwalkan ulang alarm mingguan
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received at ${Date()}")

        val taskId = intent.getStringExtra("taskId")
        val scheduleId = intent.getStringExtra("scheduleId")
        val title = intent.getStringExtra("title") ?: "Pengingat"
        val message = intent.getStringExtra("message") ?: "Waktunya untuk aktivitas terjadwal Anda"
        val isSchedule = intent.getBooleanExtra("isSchedule", false)
        val recurringWeek = intent.getIntExtra("recurringWeek", -1)

        val notificationService = NotificationService(context)

        try {
            when {
                taskId != null -> {
                    Log.d(TAG, "Showing task notification: $title")
                    notificationService.sendTaskNotification(title, message, taskId, true)
                }
                scheduleId != null -> {
                    Log.d(TAG, "Showing schedule notification: $title")
                    notificationService.sendScheduleNotification(title, message, scheduleId)

                    if (isSchedule && recurringWeek != -1) {
                        rescheduleWeeklyAlarm(context, scheduleId, recurringWeek)
                    }
                }
                else -> {
                    Log.d(TAG, "Showing general notification: $title")
                    notificationService.sendGeneralNotification(title, message)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}")
        }
    }

    /**
     * Jadwalkan ulang alarm mingguan setelah alarm saat ini dijalankan
     */
    private fun rescheduleWeeklyAlarm(context: Context, scheduleId: String, currentWeek: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = FirestoreRepository()
                val schedules = repository.getSchedules()
                val schedule = schedules.find { it.id == scheduleId }

                if (schedule != null) {
                    val futureWeek = currentWeek + 4

                    Log.d(TAG, "Rescheduling alarm for ${schedule.matkul} for week $futureWeek")

                    val dayOfWeek = when (schedule.hari) {
                        "Senin" -> Calendar.MONDAY
                        "Selasa" -> Calendar.TUESDAY
                        "Rabu" -> Calendar.WEDNESDAY
                        "Kamis" -> Calendar.THURSDAY
                        "Jumat" -> Calendar.FRIDAY
                        "Sabtu" -> Calendar.SATURDAY
                        "Minggu" -> Calendar.SUNDAY
                        else -> {
                            Log.e(TAG, "Invalid day: ${schedule.hari}")
                            return@launch
                        }
                    }

                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, schedule.waktuMulai.toDate().hours)
                    calendar.set(Calendar.MINUTE, schedule.waktuMulai.toDate().minutes)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    calendar.add(Calendar.WEEK_OF_YEAR, 4)

                    val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
                    val timeBefore = prefs.getString("scheduleTimeBefore", "30 Menit sebelum") ?: "30 Menit sebelum"
                    val delayMinutes = when (timeBefore) {
                        "10 Menit sebelum" -> 10
                        "30 Menit sebelum" -> 30
                        "1 Jam sebelum" -> 60
                        "1 Hari sebelum" -> 24 * 60
                        else -> 30
                    }

                    calendar.add(Calendar.MINUTE, -delayMinutes)

                    val alarmHelper = AlarmHelper(context)
                    val intent = Intent(context, AlarmReceiver::class.java).apply {
                        putExtra("scheduleId", scheduleId)
                        putExtra("title", "Pengingat Jadwal: ${schedule.matkul}")
                        putExtra("message", "Jadwal di ${schedule.ruangan} dimulai pukul ${
                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(schedule.waktuMulai.toDate())
                        }")
                        putExtra("isSchedule", true)
                        putExtra("recurringWeek", futureWeek)
                    }

                    val uniqueId = ("schedule_${scheduleId}_$futureWeek").hashCode()
                    val pendingIntent = android.app.PendingIntent.getBroadcast(
                        context,
                        uniqueId,
                        intent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }

                    Log.d(TAG, "Successfully rescheduled alarm for week $futureWeek at ${calendar.time}")
                } else {
                    Log.w(TAG, "Schedule with ID $scheduleId not found, skipping rescheduling")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarm: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}