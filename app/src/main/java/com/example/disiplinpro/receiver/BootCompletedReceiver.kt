package com.example.disiplinpro.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.disiplinpro.data.repository.FirestoreRepository
import com.example.disiplinpro.viewmodel.notification.NotificationViewModel
import com.example.disiplinpro.viewmodel.task.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver untuk mendeteksi boot completed dan memulihkan notifikasi terjadwal
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Boot completed detected, rescheduling notifications")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    if (firebaseAuth.currentUser == null) {
                        Log.d(TAG, "User not logged in, skipping notification restoration")
                        return@launch
                    }

                    Log.d(TAG, "User logged in, restoring notifications")

                    val notificationViewModel = NotificationViewModel()
                    val taskViewModel = TaskViewModel()

                    val repository = FirestoreRepository()
                    val tasks = repository.getTasks()
                    val schedules = repository.getSchedules()

                    Log.d(TAG, "Found ${tasks.size} tasks and ${schedules.size} schedules")

                    tasks.filter { !it.isCompleted }.forEach { task ->
                        Log.d(TAG, "Rescheduling notification for task: ${task.judulTugas}")
                        notificationViewModel.scheduleNotification(context, task)
                    }

                    schedules.forEach { schedule ->
                        Log.d(TAG, "Rescheduling notification for schedule: ${schedule.matkul}")
                        notificationViewModel.scheduleNotification(context, schedule)
                    }

                    Log.d(TAG, "Notification restoration completed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring notifications after boot: ${e.message}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}