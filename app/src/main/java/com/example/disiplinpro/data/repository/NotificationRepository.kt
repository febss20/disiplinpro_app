package com.example.disiplinpro.data.repository

import android.util.Log
import com.example.disiplinpro.data.model.NotificationHistory
import com.example.disiplinpro.data.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _notificationHistoryFlow = MutableStateFlow<List<NotificationHistory>>(emptyList())
    val notificationHistoryFlow: Flow<List<NotificationHistory>> = _notificationHistoryFlow.asStateFlow()

    companion object {
        private const val TAG = "NotificationRepo"
    }

    /**
     * Ambil semua riwayat notifikasi untuk pengguna saat ini
     */
    suspend fun getAllNotificationHistory(): List<NotificationHistory> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext emptyList()
            val existingNotifications = getExistingNotifications(userId)
            val generatedNotifications = generateNotificationsFromUserData(userId)

            val allNotifications = (existingNotifications + generatedNotifications)
                .sortedByDescending { it.timestamp }
                .distinctBy { it.id }

            _notificationHistoryFlow.value = allNotifications

            return@withContext allNotifications
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notification history: ${e.message}")
            return@withContext emptyList()
        }
    }

    /**
     * Ambil notifikasi yang sudah ada di Firestore
     */
    private suspend fun getExistingNotifications(userId: String): List<NotificationHistory> {
        return try {
            val snapshots = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val title = doc.getString("title") ?: ""
                    val message = doc.getString("message") ?: ""
                    val timestamp = doc.getDate("timestamp") ?: Date()
                    val typeStr = doc.getString("type") ?: "SYSTEM"
                    val type = NotificationType.valueOf(typeStr)
                    val relatedItemId = doc.getString("relatedItemId") ?: ""
                    val relatedItemTitle = doc.getString("relatedItemTitle") ?: ""
                    val isRead = doc.getBoolean("isRead") ?: false

                    NotificationHistory(
                        id = id,
                        title = title,
                        message = message,
                        timestamp = timestamp,
                        type = type,
                        relatedItemId = relatedItemId,
                        relatedItemTitle = relatedItemTitle,
                        isRead = isRead
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing notification: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting existing notifications: ${e.message}")
            emptyList()
        }
    }

    /**
     * Generate notifikasi dari task dan schedule yang sudah ditambahkan user
     */
    private suspend fun generateNotificationsFromUserData(userId: String): List<NotificationHistory> {
        val notifications = mutableListOf<NotificationHistory>()

        try {
            val readNotificationIds = getReadNotificationIds(userId)
            val deletedNotificationIds = getDeletedNotificationIds(userId)
            val currentTime = Date()

            // BAGIAN 1: TUGAS YANG SUDAH LEWAT, SELESAI, ATAU WAKTUNYA SUDAH MENCAPAI NOTIFIKASI

            val taskNotificationMinutes = 10

            val taskSnapshots = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .get()
                .await()

            val taskNotifications = taskSnapshots.documents.mapNotNull { doc ->
                try {
                    val taskId = doc.id
                    val judulTugas = doc.getString("judulTugas") ?: "Tugas"
                    val matkul = doc.getString("matkul") ?: ""
                    val tanggal = doc.getTimestamp("tanggal")?.toDate() ?: return@mapNotNull null
                    val waktu = doc.getTimestamp("waktu")?.toDate() ?: return@mapNotNull null
                    val completed = doc.getBoolean("completed") ?: false

                    val notificationId = "task_${taskId}_${tanggal.time}"

                    if (deletedNotificationIds.contains(notificationId)) {
                        return@mapNotNull null
                    }

                    val isOverdue = waktu.before(currentTime)

                    val notificationTime = Calendar.getInstance()
                    notificationTime.time = waktu
                    notificationTime.add(Calendar.MINUTE, -taskNotificationMinutes)

                    val isNotificationTime = notificationTime.time.before(currentTime)

                    if (!completed && !isOverdue && !isNotificationTime) {
                        return@mapNotNull null
                    }

                    val message = when {
                        completed -> "Tugas $judulTugas telah diselesaikan pada ${formatDateTime(waktu)}"
                        isOverdue -> "Tenggat waktu tugas $judulTugas telah berakhir pada ${formatDateTime(waktu)}"
                        else -> {
                            val diffMinutes = (waktu.time - currentTime.time) / (60 * 1000)

                            if (diffMinutes <= 60) {
                                "Tugas $judulTugas memiliki tenggat waktu ${diffMinutes.toInt()} menit lagi pada ${formatDateTime(waktu)}"
                            } else if (diffMinutes <= 24 * 60) {
                                val hours = diffMinutes / 60
                                "Tugas $judulTugas memiliki tenggat waktu ${hours.toInt()} jam lagi pada ${formatDateTime(waktu)}"
                            } else {
                                "Tugas $judulTugas memiliki tenggat waktu pada ${formatDateTime(waktu)}"
                            }
                        }
                    }

                    val title = when {
                        completed -> "Tugas Selesai: $judulTugas"
                        isOverdue -> "Tenggat Terlewat: $judulTugas"
                        else -> "Pengingat Tugas: $judulTugas"
                    }

                    NotificationHistory(
                        id = notificationId,
                        title = title,
                        message = message,
                        timestamp = if (completed || isOverdue) waktu else notificationTime.time,
                        type = NotificationType.TASK,
                        relatedItemId = taskId,
                        relatedItemTitle = if (matkul.isEmpty()) judulTugas else matkul,
                        isRead = readNotificationIds.contains(notificationId)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing task for notification: ${e.message}")
                    null
                }
            }
            notifications.addAll(taskNotifications)

            // BAGIAN 2: JADWAL YANG SUDAH BERLALU ATAU WAKTUNYA SUDAH MENCAPAI NOTIFIKASI

            val scheduleNotificationMinutes = 10

            val scheduleSnapshots = firestore.collection("users")
                .document(userId)
                .collection("schedules")
                .orderBy("waktuMulai", Query.Direction.DESCENDING)
                .get()
                .await()

            val scheduleNotifications = scheduleSnapshots.documents.mapNotNull { doc ->
                try {
                    val scheduleId = doc.id
                    val matkul = doc.getString("matkul") ?: "Kuliah"
                    val dosen = doc.getString("dosen") ?: ""
                    val ruangan = doc.getString("ruangan") ?: ""
                    val hari = doc.getString("hari") ?: ""
                    val waktuMulai = doc.getTimestamp("waktuMulai")?.toDate() ?: return@mapNotNull null

                    val notificationId = "schedule_${scheduleId}_${waktuMulai.time}"

                    if (deletedNotificationIds.contains(notificationId)) {
                        return@mapNotNull null
                    }

                    val isPast = waktuMulai.before(currentTime)

                    val notificationTime = Calendar.getInstance()
                    notificationTime.time = waktuMulai
                    notificationTime.add(Calendar.MINUTE, -scheduleNotificationMinutes)

                    val isNotificationTime = notificationTime.time.before(currentTime)

                    if (!isPast && !isNotificationTime) {
                        return@mapNotNull null
                    }

                    val extraInfo = if (ruangan.isNotEmpty()) " di ruangan $ruangan" else ""

                    val message = if (isPast) {
                        "Jadwal kuliah $matkul telah berlangsung pada ${formatDateTime(waktuMulai)}$extraInfo"
                    } else {
                        // Hitung selisih waktu antara waktu saat ini dan jadwal
                        val diffMinutes = (waktuMulai.time - currentTime.time) / (60 * 1000)

                        if (diffMinutes <= 60) {
                            "Jadwal kuliah $matkul akan dimulai ${diffMinutes.toInt()} menit lagi pada ${formatDateTime(waktuMulai)}$extraInfo"
                        } else if (diffMinutes <= 24 * 60) {
                            val hours = diffMinutes / 60
                            "Jadwal kuliah $matkul akan dimulai ${hours.toInt()} jam lagi pada ${formatDateTime(waktuMulai)}$extraInfo"
                        } else {
                            "Jadwal kuliah $matkul akan berlangsung pada ${formatDateTime(waktuMulai)}$extraInfo"
                        }
                    }

                    val title = if (isPast) "Jadwal Selesai: $matkul" else "Jadwal Dimulai: $matkul"

                    NotificationHistory(
                        id = notificationId,
                        title = title,
                        message = message,
                        timestamp = if (isPast) waktuMulai else notificationTime.time,
                        type = NotificationType.SCHEDULE,
                        relatedItemId = scheduleId,
                        relatedItemTitle = matkul,
                        isRead = readNotificationIds.contains(notificationId)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing schedule for notification: ${e.message}")
                    null
                }
            }
            notifications.addAll(scheduleNotifications)

            // BAGIAN 3: NOTIFIKASI SISTEM (Jika ada)

        } catch (e: Exception) {
            Log.e(TAG, "Error generating notifications from user data: ${e.message}")
        }

        return notifications
    }

    /**
     * Dapatkan daftar ID notifikasi yang sudah ditandai sebagai dibaca
     */
    private suspend fun getReadNotificationIds(userId: String): Set<String> {
        return try {
            val snapshots = firestore.collection("users")
                .document(userId)
                .collection("notification_read_status")
                .get()
                .await()

            snapshots.documents.mapNotNull { it.id }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting read notification IDs: ${e.message}")
            emptySet()
        }
    }

    /**
     * Dapatkan daftar ID notifikasi yang telah dihapus secara permanen
     */
    private suspend fun getDeletedNotificationIds(userId: String): Set<String> {
        return try {
            val snapshots = firestore.collection("users")
                .document(userId)
                .collection("notification_deleted")
                .get()
                .await()

            snapshots.documents.mapNotNull { it.id }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting deleted notification IDs: ${e.message}")
            emptySet()
        }
    }

    /**
     * Format tanggal dan waktu ke string yang mudah dibaca
     */
    private fun formatDateTime(date: Date): String {
        val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        return formatter.format(date)
    }

    /**
     * Hapus notifikasi spesifik
     */
    suspend fun deleteNotification(notificationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false

            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notificationId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.d(TAG, "Notification not found in main collection: $notificationId")
            }

            if (notificationId.startsWith("task_") || notificationId.startsWith("schedule_")) {
                firestore.collection("users")
                    .document(userId)
                    .collection("notification_deleted")
                    .document(notificationId)
                    .set(mapOf("timestamp" to Date()))
                    .await()
            }

            val currentList = _notificationHistoryFlow.value
            val updatedList = currentList.filter { it.id != notificationId }
            _notificationHistoryFlow.value = updatedList

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification: ${e.message}")
            false
        }
    }

    /**
     * Tandai notifikasi sebagai telah dibaca
     */
    suspend fun markNotificationAsRead(notificationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false

            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notificationId)
                    .update("isRead", true)
                    .await()
            } catch (e: Exception) {
                firestore.collection("users")
                    .document(userId)
                    .collection("notification_read_status")
                    .document(notificationId)
                    .set(mapOf("timestamp" to Date()))
                    .await()
            }

            val currentList = _notificationHistoryFlow.value
            val updatedList = currentList.map {
                if (it.id == notificationId) {
                    it.copy(isRead = true)
                } else {
                    it
                }
            }
            _notificationHistoryFlow.value = updatedList

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: ${e.message}")
            false
        }
    }

    /**
     * Hapus semua notifikasi yang sudah dibaca
     */
    suspend fun clearReadNotifications(): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false

            val batch = firestore.batch()
            val snapshots = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", true)
                .get()
                .await()

            snapshots.documents.forEach { doc ->
                batch.delete(
                    firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(doc.id)
                )
            }

            batch.commit().await()

            val readNotifications = _notificationHistoryFlow.value.filter { it.isRead }

            val dynamicBatch = firestore.batch()
            var batchCount = 0

            readNotifications.forEach { notification ->
                val notificationId = notification.id
                if (notificationId.startsWith("task_") || notificationId.startsWith("schedule_")) {
                    dynamicBatch.set(
                        firestore.collection("users")
                            .document(userId)
                            .collection("notification_deleted")
                            .document(notificationId),
                        mapOf("timestamp" to Date(), "reason" to "cleared_read")
                    )

                    batchCount++

                    if (batchCount >= 450) {
                        dynamicBatch.commit().await()
                        batchCount = 0
                    }
                }
            }

            if (batchCount > 0) {
                dynamicBatch.commit().await()
            }

            val currentList = _notificationHistoryFlow.value
            val updatedList = currentList.filter { !it.isRead }
            _notificationHistoryFlow.value = updatedList

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing read notifications: ${e.message}")
            false
        }
    }
}