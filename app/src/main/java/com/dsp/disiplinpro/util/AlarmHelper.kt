package com.dsp.disiplinpro.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.dsp.disiplinpro.data.model.Task
import com.dsp.disiplinpro.data.model.Schedule
import com.dsp.disiplinpro.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Helper class untuk mengelola alarm menggunakan AlarmManager
 * Digunakan sebagai cadangan untuk WorkManager untuk memastikan notifikasi tepat waktu
 */
class AlarmHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Jadwalkan alarm untuk tugas dengan deadline
     */
    fun scheduleTaskAlarm(task: Task) {
        if (task.isCompleted || task.completed == true) {
            Log.d(TAG, "Not scheduling alarm for completed task: ${task.judulTugas}")
            return
        }

        val deadlineTime = task.waktu.toDate().time
        val currentTime = System.currentTimeMillis()

        if (deadlineTime <= currentTime) {
            Log.d(TAG, "Not scheduling alarm for past deadline: ${task.judulTugas}")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("title", "Pengingat Tugas: ${task.judulTugas}")
            putExtra("message", "Tugas ${task.judulTugas} untuk mata kuliah ${task.matkul} jatuh tempo pada ${
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(task.waktu.toDate())
            }")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(deadlineTime, pendingIntent)
        Log.d(TAG, "Scheduled alarm for task: ${task.judulTugas} at ${Date(deadlineTime)}")
    }

    /**
     * Jadwalkan alarm untuk jadwal (schedule)
     */
    fun scheduleScheduleAlarm(schedule: Schedule, triggerTimeMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("scheduleId", schedule.id)
            putExtra("title", "Pengingat Jadwal: ${schedule.matkul}")
            putExtra("message", "Jadwal di ${schedule.ruangan} dimulai pukul ${
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())
            }")
            putExtra("isSchedule", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "schedule_${schedule.id}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(triggerTimeMillis, pendingIntent)
        Log.d(TAG, "Scheduled alarm for schedule: ${schedule.matkul} at ${Date(triggerTimeMillis)}")
    }

    /**
     * Jadwalkan alarm untuk jadwal mingguan (lebih andal dari PeriodicWorkRequest)
     * Ini menjadwalkan alarm untuk beberapa minggu ke depan
     */
    fun scheduleRecurringWeeklyAlarm(schedule: Schedule, dayOfWeek: Int, hourOfDay: Int, minute: Int, delayMinutes: Int) {
        val calendar = Calendar.getInstance()

        if (calendar.get(Calendar.DAY_OF_WEEK) > dayOfWeek) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)

        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.add(Calendar.MINUTE, -delayMinutes)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        for (week in 0..3) {
            val targetTime = calendar.timeInMillis + (week * 7 * 24 * 60 * 60 * 1000L)
            val uniqueId = ("schedule_${schedule.id}_${week}").hashCode()

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("scheduleId", schedule.id)
                putExtra("title", "Pengingat Jadwal: ${schedule.matkul}")
                putExtra("message", "Jadwal di ${schedule.ruangan} dimulai pukul ${
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())
                }")
                putExtra("isSchedule", true)
                putExtra("recurringWeek", week)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleAlarm(targetTime, pendingIntent)

            Log.d(TAG, "Scheduled recurring alarm for ${schedule.matkul} (week $week) at ${Date(targetTime)}")
        }
    }

    /**
     * Batalkan alarm untuk tugas berdasarkan ID
     */
    fun cancelTaskAlarm(taskId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled alarm for task ID: $taskId")
        }
    }

    /**
     * Batalkan alarm untuk jadwal berdasarkan ID
     */
    fun cancelScheduleAlarm(scheduleId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "schedule_$scheduleId".hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled alarm for schedule ID: $scheduleId")
        }
    }

    /**
     * Jadwalkan alarm dengan AlarmManager, menangani perbedaan versi Android
     */
    private fun scheduleAlarm(triggerTimeMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Untuk Android 6.0+
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    companion object {
        private const val TAG = "AlarmHelper"
    }
}