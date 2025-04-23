package com.example.disiplinpro.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.disiplinpro.service.NotificationService

/**
 * BroadcastReceiver untuk menerima alarm dari AlarmManager
 * dan menampilkan notifikasi saat waktunya tiba
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received")

        // Ambil data dari intent
        val taskId = intent.getStringExtra("taskId")
        val scheduleId = intent.getStringExtra("scheduleId")
        val title = intent.getStringExtra("title") ?: "Pengingat"
        val message = intent.getStringExtra("message") ?: "Waktunya untuk aktivitas terjadwal Anda"
        val isSchedule = intent.getBooleanExtra("isSchedule", false)

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

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}