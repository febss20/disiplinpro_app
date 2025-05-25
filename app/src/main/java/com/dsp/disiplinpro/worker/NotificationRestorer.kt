package com.dsp.disiplinpro.worker

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dsp.disiplinpro.data.repository.FirestoreRepository
import com.dsp.disiplinpro.viewmodel.notification.NotificationViewModel
import com.dsp.disiplinpro.viewmodel.task.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NotificationRestorer(private val context: Context) {

    suspend fun checkAndRestoreScheduledNotifications() = withContext(Dispatchers.IO) {
        try {
            val activeWorkInfos = WorkManager.getInstance(context).getWorkInfosByTag("notification_tag").get()

            if (activeWorkInfos.isEmpty()) {
                Log.d(TAG, "Tidak ada pekerjaan terjadwal aktif, memulihkan notifikasi...")

                val notificationViewModel = NotificationViewModel()
                val taskViewModel = TaskViewModel()

                val repository = FirestoreRepository()
                val tasks = repository.getTasks()
                val schedules = repository.getSchedules()

                tasks.filter { !it.isCompleted }.forEach { task ->
                    Log.d(TAG, "Memulihkan notifikasi untuk tugas: ${task.judulTugas}")
                    notificationViewModel.scheduleNotification(context, task)
                }

                schedules.forEach { schedule ->
                    Log.d(TAG, "Memulihkan notifikasi untuk jadwal: ${schedule.matkul}")
                    notificationViewModel.scheduleNotification(context, schedule)
                }

                scheduleNotificationHealthCheck()

                Log.d(TAG, "Pemulihan notifikasi selesai")
            } else {
                Log.d(TAG, "Ditemukan ${activeWorkInfos.size} pekerjaan terjadwal aktif, pemulihan tidak diperlukan")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saat memeriksa/memulihkan notifikasi: ${e.message}")
        }
    }

    private fun scheduleNotificationHealthCheck() {
        val healthCheckRequest = OneTimeWorkRequestBuilder<NotificationHealthCheckWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .addTag("health_check_tag")
            .build()

        WorkManager.getInstance(context)
            .enqueue(healthCheckRequest)

        Log.d(TAG, "Pemeriksaan kesehatan notifikasi dijadwalkan untuk 24 jam berikutnya")
    }

    companion object {
        private const val TAG = "NotificationRestorer"
    }
}