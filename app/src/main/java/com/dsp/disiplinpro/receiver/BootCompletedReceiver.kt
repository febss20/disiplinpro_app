package com.dsp.disiplinpro.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dsp.disiplinpro.data.repository.FirestoreRepository
import com.dsp.disiplinpro.viewmodel.notification.NotificationViewModel
import com.dsp.disiplinpro.viewmodel.task.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import com.dsp.disiplinpro.worker.NotificationHealthCheckWorker
import java.util.concurrent.TimeUnit

/**
 * BroadcastReceiver untuk mendeteksi boot completed dan memulihkan notifikasi terjadwal
 * Mencegah notifikasi hilang setelah restart perangkat
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Boot completed detected at ${java.util.Date()}, memulai proses pemulihan notifikasi")

            scheduleRestoreWork(context)
        }
    }

    /**
     * Jadwalkan pemulihan notifikasi dalam worker terpisah untuk menghindari ANR
     * dan memastikan pemulihan dilakukan dengan benar
     */
    private fun scheduleRestoreWork(context: Context) {
        val inputData = Data.Builder()
            .putString("source", "boot_completed")
            .putLong("timestamp", System.currentTimeMillis())
            .build()

        val restoreWorkRequest = OneTimeWorkRequestBuilder<NotificationHealthCheckWorker>()
            .setInitialDelay(30, TimeUnit.SECONDS)
            .setInputData(inputData)
            .addTag("restore_notification_tag")
            .build()

        WorkManager.getInstance(context)
            .enqueue(restoreWorkRequest)

        Log.d(TAG, "Pemulihan notifikasi dijadwalkan untuk dijalankan dalam 30 detik")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                if (firebaseAuth.currentUser == null) {
                    Log.d(TAG, "User not logged in, skipping notification restoration")
                    return@launch
                }

                Log.d(TAG, "User logged in, memulai proses pemulihan")

                val notificationViewModel = NotificationViewModel()
                val taskViewModel = TaskViewModel()

                val repository = FirestoreRepository()
                val tasks = repository.getTasks()
                val schedules = repository.getSchedules()

                Log.d(TAG, "Ditemukan ${tasks.size} tugas dan ${schedules.size} jadwal")

                var nonCompletedCount = 0
                tasks.filter { !it.isCompleted }.forEach { task ->
                    nonCompletedCount++
                    Log.d(TAG, "Memulihkan notifikasi untuk tugas: ${task.judulTugas}")
                    notificationViewModel.scheduleNotification(context, task)
                }

                schedules.forEach { schedule ->
                    Log.d(TAG, "Memulihkan notifikasi untuk jadwal: ${schedule.matkul}")
                    notificationViewModel.scheduleNotification(context, schedule)
                }

                Log.d(TAG, "Pemulihan selesai: $nonCompletedCount tugas aktif dan ${schedules.size} jadwal")
            } catch (e: Exception) {
                Log.e(TAG, "Error saat pemulihan notifikasi: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}