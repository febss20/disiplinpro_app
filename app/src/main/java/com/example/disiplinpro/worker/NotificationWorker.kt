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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME_PREFIX = "notification_"
        const val OVERDUE_WORK_NAME_PREFIX = "overdue_notification_"
        const val CHANNEL_ID = "disiplinpro_channel"
    }

    override suspend fun doWork(): Result {
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("NotificationWorker", "User login status: ${user != null}")
        if (user == null) {
            Log.d("NotificationWorker", "User not logged in, skipping notification")
            return Result.success()
        }

        Log.d("NotificationWorker", "Pekerjaan dijalankan")
        val title = inputData.getString("title") ?: "Pengingat"
        val message = inputData.getString("message") ?: "Waktu untuk memulai!"
        val scheduleId = inputData.getString("scheduleId")
        val taskId = inputData.getString("taskId")
        val isOverdue = inputData.getBoolean("isOverdue", false)

        Log.d("NotificationWorker", "Processing notification: $title, $message, scheduleId=$scheduleId, taskId=$taskId, isOverdue=$isOverdue")

        if (isOverdue && taskId != null) {
            val isTaskCompleted = checkTaskCompletionStatus(taskId)
            if (isTaskCompleted) {
                Log.d("NotificationWorker", "Task $taskId already completed, skipping overdue notification")
                return Result.success()
            }
        }

        createNotificationChannel()
        showNotification(title, message)
        Log.d("NotificationWorker", "Notifikasi ditampilkan")

        return Result.success()
    }

    private suspend fun checkTaskCompletionStatus(taskId: String): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            val isCompleted = snapshot.getBoolean("isCompleted") ?: false
            val completed = snapshot.getBoolean("completed") ?: false

            return isCompleted || completed
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error checking task completion status: ${e.message}")
            return false
        }
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