package com.dsp.disiplinpro.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dsp.disiplinpro.data.repository.FirestoreRepository
import com.dsp.disiplinpro.viewmodel.notification.NotificationViewModel
import com.dsp.disiplinpro.viewmodel.task.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Worker untuk memeriksa kesehatan notifikasi terjadwal
 * Menjalankan pemeriksaan rutin dan memulihkan notifikasi yang mungkin hilang
 */
class NotificationHealthCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Pemeriksaan kesehatan notifikasi dimulai")

            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser == null) {
                Log.d(TAG, "User tidak login, melewati pemeriksaan kesehatan")
                return@withContext Result.success()
            }

            val repository = FirestoreRepository()
            val tasks = repository.getTasks()
            val schedules = repository.getSchedules()

            val pendingTasks = tasks.filter { !it.isCompleted }

            Log.d(TAG, "Ditemukan ${pendingTasks.size} tugas aktif dan ${schedules.size} jadwal")

            val notificationViewModel = NotificationViewModel()
            val taskViewModel = TaskViewModel()

            pendingTasks.forEach { task ->
                notificationViewModel.scheduleNotification(context, task)
            }

            schedules.forEach { schedule ->
                notificationViewModel.scheduleNotification(context, schedule)
            }

            Log.d(TAG, "Pemulihan notifikasi selesai, memulai pemeriksaan berikutnya dalam 24 jam")

            scheduleNextHealthCheck()

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error saat pemeriksaan kesehatan notifikasi: ${e.message}")
            Result.retry()
        }
    }

    private fun scheduleNextHealthCheck() {
        val healthCheckRequest = OneTimeWorkRequestBuilder<NotificationHealthCheckWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .addTag("health_check_tag")
            .build()

        WorkManager.getInstance(context)
            .enqueue(healthCheckRequest)
    }

    companion object {
        private const val TAG = "NotificationHealthCheck"
    }
}