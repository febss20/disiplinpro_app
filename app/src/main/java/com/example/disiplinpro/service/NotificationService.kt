package com.example.disiplinpro.service

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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.disiplinpro.MainActivity
import com.example.disiplinpro.R
import com.example.disiplinpro.worker.NotificationWorker

/**
 * Service class to handle manual notification creation and display.
 * This can be used anywhere in the app to send immediate notifications.
 */
class NotificationService(private val context: Context) {

    companion object {
        private const val TAG = "NotificationService"

        // Use the same channel IDs as the NotificationWorker for consistency
        private const val CHANNEL_ID = NotificationWorker.CHANNEL_ID
        private const val CHANNEL_ID_HIGH = NotificationWorker.CHANNEL_ID_HIGH

        // Group key for notification grouping
        private const val GROUP_KEY = "com.example.disiplinpro.NOTIFICATIONS"
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Default channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DisiplinPro Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel untuk notifikasi jadwal dan tugas"
            }

            // High priority channel for urgent notifications
            val highPriorityChannel = NotificationChannel(
                CHANNEL_ID_HIGH,
                "DisiplinPro Urgent Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk notifikasi penting dan mendesak"
                enableVibration(true)
                enableLights(true)
            }

            // Register the channels with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(highPriorityChannel)

            Log.d(TAG, "Notification channels created")
        }
    }

    /**
     * Send a task notification that when clicked will open the task details screen
     */
    fun sendTaskNotification(title: String, message: String, taskId: String, isUrgent: Boolean = false) {
        if (!checkNotificationPermission()) return

        // Create an Intent that will open the MainActivity and navigate to the task details
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationWorker.EXTRA_NOTIFICATION_TYPE, NotificationWorker.TYPE_TASK)
            putExtra(NotificationWorker.EXTRA_ID, taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Always use high priority for floating notifications
        val channelId = CHANNEL_ID_HIGH
        val priority = NotificationCompat.PRIORITY_HIGH

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY)
            .setAutoCancel(true)

        // Add full-screen intent for heads-up display
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationWorker.EXTRA_NOTIFICATION_TYPE, NotificationWorker.TYPE_TASK)
            putExtra(NotificationWorker.EXTRA_ID, taskId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode() + 1000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setFullScreenIntent(fullScreenPendingIntent, true)

        // Use a stable notification ID
        val notificationId = "task_$taskId".hashCode()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d(TAG, "Task notification sent: $title, ID: $notificationId")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to send notification: ${e.message}")
        }
    }

    /**
     * Send a schedule notification that when clicked will open the schedule details screen
     */
    fun sendScheduleNotification(title: String, message: String, scheduleId: String) {
        if (!checkNotificationPermission()) return

        // Create an Intent that will open the MainActivity and navigate to the schedule details
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationWorker.EXTRA_NOTIFICATION_TYPE, NotificationWorker.TYPE_SCHEDULE)
            putExtra(NotificationWorker.EXTRA_ID, scheduleId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Always use high priority for floating notifications
        val channelId = CHANNEL_ID_HIGH
        val priority = NotificationCompat.PRIORITY_HIGH

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY)
            .setAutoCancel(true)

        // Add full-screen intent for heads-up display
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationWorker.EXTRA_NOTIFICATION_TYPE, NotificationWorker.TYPE_SCHEDULE)
            putExtra(NotificationWorker.EXTRA_ID, scheduleId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.hashCode() + 1000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setFullScreenIntent(fullScreenPendingIntent, true)

        // Use a stable notification ID
        val notificationId = "schedule_$scheduleId".hashCode()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d(TAG, "Schedule notification sent: $title, ID: $notificationId")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to send notification: ${e.message}")
        }
    }

    /**
     * Send a general notification with a custom action
     */
    fun sendGeneralNotification(title: String, message: String, notificationId: Int = System.currentTimeMillis().toInt()) {
        if (!checkNotificationPermission()) return

        // Create a simple Intent to open the main activity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Always use high priority for floating notifications
        val channelId = CHANNEL_ID_HIGH
        val priority = NotificationCompat.PRIORITY_HIGH

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY)
            .setAutoCancel(true)

        // Add full-screen intent for heads-up display
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            1000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setFullScreenIntent(fullScreenPendingIntent, true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d(TAG, "General notification sent: $title, ID: $notificationId")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to send notification: ${e.message}")
        }
    }

    /**
     * Create a summary notification that groups all other notifications
     * This is required for Android 7.0+ to properly show grouped notifications
     */
    fun createSummaryNotification() {
        if (!checkNotificationPermission()) return

        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("DisiplinPro Notifications")
            .setContentText("You have multiple notifications")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(0, summaryNotification)
            Log.d(TAG, "Summary notification sent")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to send summary notification: ${e.message}")
        }
    }

    /**
     * Cancel a specific notification
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
        Log.d(TAG, "Notification cancelled: $notificationId")
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
        Log.d(TAG, "All notifications cancelled")
    }

    /**
     * Check if the app has notification permissions
     */
    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted")
                return false
            }
        }
        return true
    }
}