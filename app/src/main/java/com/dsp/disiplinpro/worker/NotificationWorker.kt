package com.dsp.disiplinpro.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dsp.disiplinpro.MainActivity
import com.dsp.disiplinpro.R
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
        const val CHANNEL_ID_HIGH = "disiplinpro_important_channel"

        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_ID = "id"
        const val TYPE_TASK = "task"
        const val TYPE_SCHEDULE = "schedule"
    }

    override suspend fun doWork(): Result {
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("NotificationWorker", "User login status: ${user != null}")
        if (user == null) {
            Log.d("NotificationWorker", "User not logged in, skipping notification")
            return Result.success()
        }

        Log.d("NotificationWorker", "Pekerjaan dijalankan pada: ${java.util.Date()}")
        val title = inputData.getString("title") ?: "Pengingat"
        val message = inputData.getString("message") ?: "Waktu untuk memulai!"
        val scheduleId = inputData.getString("scheduleId")
        val taskId = inputData.getString("taskId")
        val isOverdue = inputData.getBoolean("isOverdue", false)

        val dayOfWeek = inputData.getInt("dayOfWeek", -1)
        val hour = inputData.getInt("hour", -1)
        val minute = inputData.getInt("minute", -1)
        val delayMinutes = inputData.getInt("delayMinutes", -1)

        val idInfo = when {
            scheduleId != null -> "scheduleId=$scheduleId"
            taskId != null -> "taskId=$taskId (isOverdue=$isOverdue)"
            else -> "no specific ID"
        }

        val scheduleInfo = if (dayOfWeek != -1 && hour != -1 && minute != -1) {
            ", dayOfWeek=$dayOfWeek, time=$hour:$minute, delayMinutes=$delayMinutes"
        } else {
            ""
        }

        Log.d("NotificationWorker", "Processing notification: $title, $idInfo$scheduleInfo")

        if (isOverdue && taskId != null) {
            val isTaskCompleted = checkTaskCompletionStatus(taskId)
            if (isTaskCompleted) {
                Log.d("NotificationWorker", "Task $taskId already completed, skipping overdue notification")
                return Result.success()
            }
        }

        try {
            createNotificationChannels()

            when {
                taskId != null -> {
                    showTaskNotification(title, message, taskId, isOverdue)
                    if (isOverdue) {
                        Log.d("NotificationWorker", "Overdue notification for task $taskId completed, no rescheduling needed")
                    }
                }
                scheduleId != null -> {
                    showScheduleNotification(title, message, scheduleId)

                    if (dayOfWeek != -1 && hour != -1 && minute != -1) {
                        Log.d("NotificationWorker", "Periodic schedule notification completed, would reschedule if needed")
                    }
                }
                else -> showSimpleNotification(title, message)
            }

            Log.d("NotificationWorker", "Notifikasi berhasil ditampilkan untuk: $title")
            return Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error showing notification: ${e.message}")
            val runAttemptCount = runAttemptCount
            return if (runAttemptCount < 3) {
                Log.w("NotificationWorker", "Akan mencoba lagi (attempt $runAttemptCount)")
                Result.retry()
            } else {
                Log.e("NotificationWorker", "Menyerah setelah $runAttemptCount percobaan")
                Result.failure()
            }
        }
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

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DisiplinPro Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel untuk notifikasi jadwal dan tugas"
            }

            val highPriorityChannel = NotificationChannel(
                CHANNEL_ID_HIGH,
                "DisiplinPro Urgent Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk notifikasi penting dan mendesak"
                enableVibration(true)
                enableLights(true)
            }

            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            manager.createNotificationChannel(highPriorityChannel)
            Log.d("NotificationWorker", "Notification channels created")
        }
    }

    private fun showTaskNotification(title: String, message: String, taskId: String, isOverdue: Boolean) {
        if (!checkNotificationPermission()) return

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_TASK)
            putExtra(EXTRA_ID, taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = CHANNEL_ID_HIGH
        val priority = NotificationCompat.PRIORITY_HIGH

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)

        val fullScreenIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_TASK)
            putExtra(EXTRA_ID, taskId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.hashCode() + 1000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notification.setFullScreenIntent(fullScreenPendingIntent, true)

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        try {
            val notificationId = "task_$taskId".hashCode()
            manager.notify(notificationId, notification.build())
            Log.d("NotificationWorker", "Task notification sent: $title, ID: $notificationId")
        } catch (e: SecurityException) {
            Log.e("NotificationWorker", "Failed to send notification: ${e.message}")
        }
    }

    private fun showScheduleNotification(title: String, message: String, scheduleId: String) {
        if (!checkNotificationPermission()) return

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_SCHEDULE)
            putExtra(EXTRA_ID, scheduleId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = CHANNEL_ID_HIGH
        val priority = NotificationCompat.PRIORITY_HIGH

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)

        val fullScreenIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_SCHEDULE)
            putExtra(EXTRA_ID, scheduleId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            scheduleId.hashCode() + 1000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notification.setFullScreenIntent(fullScreenPendingIntent, true)

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        try {
            val notificationId = "schedule_$scheduleId".hashCode()
            manager.notify(notificationId, notification.build())
            Log.d("NotificationWorker", "Schedule notification sent: $title, ID: $notificationId")
        } catch (e: SecurityException) {
            Log.e("NotificationWorker", "Failed to send notification: ${e.message}")
        }
    }

    private fun showSimpleNotification(title: String, message: String) {
        if (!checkNotificationPermission()) return

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = CHANNEL_ID_HIGH
        val priority = NotificationCompat.PRIORITY_HIGH

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)

        val fullScreenIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            1000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notification.setFullScreenIntent(fullScreenPendingIntent, true)

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        try {
            manager.notify(System.currentTimeMillis().toInt(), notification.build())
            Log.d("NotificationWorker", "Simple notification sent: $title")
        } catch (e: SecurityException) {
            Log.e("NotificationWorker", "Failed to send notification: ${e.message}")
        }
    }

    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationWorker", "Notification permission not granted")
                return false
            }
        }
        return true
    }
}