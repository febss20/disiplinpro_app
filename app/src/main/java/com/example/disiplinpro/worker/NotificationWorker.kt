package com.example.disiplinpro.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.disiplinpro.R

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME_PREFIX = "notification_"
        const val CHANNEL_ID = "disiplinpro_channel"
    }

    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Pekerjaan dijalankan")
        val title = inputData.getString("title") ?: "Pengingat"
        val message = inputData.getString("message") ?: "Waktu untuk memulai!"
        val scheduleId = inputData.getString("scheduleId")

        Log.d("NotificationWorker", "Processing notification: $title, $message, scheduleId=$scheduleId")

        createNotificationChannel()
        showNotification(title, message)
        Log.d("NotificationWorker", "Notifikasi ditampilkan")

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DisiplinPro Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel untuk notifikasi jadwal dan tugas"
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d("NotificationWorker", "Notification channel created: $CHANNEL_ID")
        }
    }

    private fun showNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationWorker", "Notification permission not granted")
                return
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        try {
            manager.notify(System.currentTimeMillis().toInt(), notification)
            Log.d("NotificationWorker", "Notification sent: $title")
        } catch (e: SecurityException) {
            Log.e("NotificationWorker", "Failed to send notification: ${e.message}")
        }
    }
}