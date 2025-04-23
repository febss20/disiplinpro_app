package com.example.disiplinpro.data.repository

import android.util.Log
import com.example.disiplinpro.data.model.Notification
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

    private val _notificationFlow = MutableStateFlow<List<Notification>>(emptyList())
    val notificationFlow: Flow<List<Notification>> = _notificationFlow.asStateFlow()

    companion object {
        private const val TAG = "NotificationRepo"

        const val SCHEDULE_PREFIX = "schedule_"
        const val TASK_PREFIX = "task_"
        const val SCHEDULE_ONGOING_SUFFIX = "_ongoing"
        const val SCHEDULE_FINISHED_SUFFIX = "_finished"
        const val TASK_COMPLETED_SUFFIX = "_completed"
    }

    suspend fun getAllNotifications(): List<Notification> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext emptyList()
            val existingNotifications = getExistingNotifications(userId)
            val generatedNotifications = generateNotificationsFromUserData(userId)

            val allNotifications = (existingNotifications + generatedNotifications)
                .sortedByDescending { it.timestamp }
                .distinctBy { it.id }

            _notificationFlow.value = allNotifications

            return@withContext allNotifications
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications: ${e.message}")
            return@withContext emptyList()
        }
    }

    private suspend fun getExistingNotifications(userId: String): List<Notification> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            return@withContext snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val title = doc.getString("title") ?: "Notifikasi"
                    val message = doc.getString("message") ?: ""
                    val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date()
                    val type = when (doc.getString("type")) {
                        "TASK" -> NotificationType.TASK
                        "SCHEDULE" -> NotificationType.SCHEDULE
                        else -> NotificationType.SYSTEM
                    }
                    val relatedItemId = doc.getString("relatedItemId") ?: ""
                    val relatedItemTitle = doc.getString("relatedItemTitle") ?: ""
                    val isRead = doc.getBoolean("isRead") ?: false

                    Notification(
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
                    Log.e(TAG, "Error parsing notification document: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching existing notifications: ${e.message}")
            emptyList()
        }
    }

    private suspend fun generateNotificationsFromUserData(userId: String): List<Notification> = withContext(Dispatchers.IO) {
        val notifications = mutableListOf<Notification>()

        try {
            val deletedNotificationsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("notification_deleted")
                .get()
                .await()

            val deletedIds = deletedNotificationsSnapshot.documents.map { it.id }

            val readNotificationsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", true)
                .get()
                .await()

            val readNotificationIds = readNotificationsSnapshot.documents.map { it.id }

            // BAGIAN 1: TUGAS

            val taskSnapshots = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .await()

            val taskNotifications = taskSnapshots.documents.mapNotNull { doc ->
                try {
                    val taskId = doc.id
                    if (taskId in deletedIds) return@mapNotNull null

                    val judulTugas = doc.getString("judulTugas") ?: "Tugas Tanpa Judul"
                    val matkul = doc.getString("matkul") ?: "Mata Kuliah Tidak Diketahui"
                    val waktu = doc.getTimestamp("waktu")?.toDate()
                    val isCompleted = doc.getBoolean("isCompleted") ?: false

                    val completedNotificationId = "$TASK_PREFIX${taskId}$TASK_COMPLETED_SUFFIX"
                    val hasCompletedNotification = completedNotificationId in readNotificationIds

                    if (waktu == null) return@mapNotNull null

                    val now = Date()

                    if (isCompleted && !hasCompletedNotification) {
                        val notificationId = completedNotificationId
                        val title = "Tugas Selesai: $judulTugas"
                        val message = "Anda telah menyelesaikan tugas $judulTugas untuk mata kuliah $matkul."

                        Notification(
                            id = notificationId,
                            title = title,
                            message = message,
                            timestamp = Date(),
                            type = NotificationType.TASK,
                            relatedItemId = taskId,
                            relatedItemTitle = judulTugas,
                            isRead = false
                        )
                    } else if (waktu.before(now) && !isCompleted) {
                        val notificationId = "$TASK_PREFIX$taskId"
                        val title = "Tugas Terlambat: $judulTugas"
                        val message = "Tugas untuk mata kuliah $matkul sudah melewati batas waktu pengumpulan."

                        Notification(
                            id = notificationId,
                            title = title,
                            message = message,
                            timestamp = waktu,
                            type = NotificationType.TASK,
                            relatedItemId = taskId,
                            relatedItemTitle = judulTugas,
                            isRead = notificationId in readNotificationIds
                        )
                    } else if (isUpcoming(waktu) && !isCompleted) {
                        val notificationId = "${TASK_PREFIX}${taskId}_upcoming"
                        val title = "Tugas Mendatang: $judulTugas"
                        val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(waktu)
                        val message = "Tugas $judulTugas untuk mata kuliah $matkul jatuh tempo pada $formattedTime."

                        Notification(
                            id = notificationId,
                            title = title,
                            message = message,
                            timestamp = Date(),
                            type = NotificationType.TASK,
                            relatedItemId = taskId,
                            relatedItemTitle = judulTugas,
                            isRead = notificationId in readNotificationIds
                        )
                    } else {
                        null
                    }
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
                .get()
                .await()

            val scheduleNotifications = scheduleSnapshots.documents.mapNotNull { doc ->
                try {
                    val scheduleId = doc.id
                    if (scheduleId in deletedIds) return@mapNotNull null

                    val matkul = doc.getString("matkul") ?: "Jadwal Tanpa Nama"
                    val ruangan = doc.getString("ruangan") ?: "Tidak Ada Ruangan"
                    val hari = doc.getString("hari")
                    val waktuMulai = doc.getTimestamp("waktuMulai")?.toDate()

                    if (hari == null || waktuMulai == null) return@mapNotNull null

                    val calendar = Calendar.getInstance()
                    val today = calendar.get(Calendar.DAY_OF_WEEK)

                    val scheduleDayOfWeek = when (hari) {
                        "Senin" -> Calendar.MONDAY
                        "Selasa" -> Calendar.TUESDAY
                        "Rabu" -> Calendar.WEDNESDAY
                        "Kamis" -> Calendar.THURSDAY
                        "Jumat" -> Calendar.FRIDAY
                        "Sabtu" -> Calendar.SATURDAY
                        "Minggu" -> Calendar.SUNDAY
                        else -> -1
                    }

                    if (scheduleDayOfWeek == -1) return@mapNotNull null

                    val scheduleCalendar = Calendar.getInstance()
                    scheduleCalendar.time = waktuMulai

                    scheduleCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                    scheduleCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                    scheduleCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))

                    var dayDiff = scheduleDayOfWeek - today
                    if (dayDiff < -3) dayDiff += 7
                    else if (dayDiff > 3) dayDiff -= 7

                    scheduleCalendar.add(Calendar.DAY_OF_YEAR, dayDiff)

                    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(waktuMulai)
                    val notificationId = "${SCHEDULE_PREFIX}$scheduleId"
                    val today_diff = Math.abs(dayDiff)

                    if (today_diff <= 1) {
                        val currentTimeMillis = System.currentTimeMillis()
                        val scheduleTimeMillis = scheduleCalendar.timeInMillis
                        val diffMillis = scheduleTimeMillis - currentTimeMillis
                        val diffMinutes = diffMillis / (60 * 1000)
                        val now = Date()

                        val waktuSelesai = doc.getTimestamp("waktuSelesai")?.toDate()
                        if (waktuSelesai == null) return@mapNotNull null

                        val selesaiCalendar = Calendar.getInstance()
                        selesaiCalendar.time = waktuSelesai
                        selesaiCalendar.set(Calendar.YEAR, scheduleCalendar.get(Calendar.YEAR))
                        selesaiCalendar.set(Calendar.MONTH, scheduleCalendar.get(Calendar.MONTH))
                        selesaiCalendar.set(Calendar.DAY_OF_MONTH, scheduleCalendar.get(Calendar.DAY_OF_MONTH))

                        val selesaiTimeMillis = selesaiCalendar.timeInMillis
                        val selesaiDiffMillis = selesaiTimeMillis - currentTimeMillis
                        val selesaiDiffMinutes = selesaiDiffMillis / (60 * 1000)

                        val ongoingNotificationId = "$notificationId$SCHEDULE_ONGOING_SUFFIX"
                        val finishedNotificationId = "$notificationId$SCHEDULE_FINISHED_SUFFIX"

                        if (diffMinutes in 0..scheduleNotificationMinutes) {
                            val title = "Jadwal Akan Dimulai: $matkul"
                            val message = "Jadwal kuliah $matkul di $ruangan akan dimulai ${if (diffMinutes <= 1) "sebentar lagi" else "dalam $diffMinutes menit"}."

                            Notification(
                                id = "${notificationId}_upcoming",
                                title = title,
                                message = message,
                                timestamp = Date(),
                                type = NotificationType.SCHEDULE,
                                relatedItemId = scheduleId,
                                relatedItemTitle = matkul,
                                isRead = "${notificationId}_upcoming" in readNotificationIds
                            )
                        } else if (diffMinutes < 0 && selesaiDiffMinutes > 0) {
                            val title = "Jadwal Sedang Berlangsung: $matkul"
                            val formattedEndTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(waktuSelesai)
                            val message = "Jadwal kuliah $matkul di $ruangan sedang berlangsung sampai $formattedEndTime."

                            Notification(
                                id = ongoingNotificationId,
                                title = title,
                                message = message,
                                timestamp = Date(Math.max(scheduleTimeMillis, now.time)),
                                type = NotificationType.SCHEDULE,
                                relatedItemId = scheduleId,
                                relatedItemTitle = matkul,
                                isRead = ongoingNotificationId in readNotificationIds
                            )
                        } else if (selesaiDiffMinutes <= 0 && selesaiDiffMinutes > -30 && finishedNotificationId !in readNotificationIds) {
                            val title = "Jadwal Selesai: $matkul"
                            val formattedStartTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(waktuMulai)
                            val formattedEndTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(waktuSelesai)
                            val message = "Jadwal kuliah $matkul di $ruangan telah selesai (${formattedStartTime}-${formattedEndTime})."

                            Notification(
                                id = finishedNotificationId,
                                title = title,
                                message = message,
                                timestamp = Date(Math.max(selesaiTimeMillis, now.time)),
                                type = NotificationType.SCHEDULE,
                                relatedItemId = scheduleId,
                                relatedItemTitle = matkul,
                                isRead = false
                            )
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing schedule for notification: ${e.message}")
                    null
                }
            }
            notifications.addAll(scheduleNotifications)

            // BAGIAN 3: JADWAL YANG AKAN DATANG DALAM WAKTU DEKAT (DALAM 2 HARI)
            val upcomingDays = 2
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val tomorrow = if (today == Calendar.SATURDAY) Calendar.SUNDAY else (today + 1)
            val dayAfterTomorrow = if (tomorrow == Calendar.SATURDAY) Calendar.SUNDAY else (tomorrow + 1)

            val upcomingDaysOfWeek = listOf(tomorrow, dayAfterTomorrow)

            val upcomingSchedules = scheduleSnapshots.documents.mapNotNull { doc ->
                try {
                    val scheduleId = doc.id
                    if (scheduleId in deletedIds) return@mapNotNull null
                    if ("schedule_${scheduleId}_upcoming" in notifications.map { it.id }) return@mapNotNull null

                    val hari = doc.getString("hari")
                    val matkul = doc.getString("matkul") ?: "Jadwal Tanpa Nama"
                    val ruangan = doc.getString("ruangan") ?: "Tidak Ada Ruangan"
                    val waktuMulai = doc.getTimestamp("waktuMulai")?.toDate()

                    if (hari == null || waktuMulai == null) return@mapNotNull null

                    val scheduleDayOfWeek = when (hari) {
                        "Senin" -> Calendar.MONDAY
                        "Selasa" -> Calendar.TUESDAY
                        "Rabu" -> Calendar.WEDNESDAY
                        "Kamis" -> Calendar.THURSDAY
                        "Jumat" -> Calendar.FRIDAY
                        "Sabtu" -> Calendar.SATURDAY
                        "Minggu" -> Calendar.SUNDAY
                        else -> -1
                    }

                    if (scheduleDayOfWeek == -1 || scheduleDayOfWeek !in upcomingDaysOfWeek) return@mapNotNull null

                    val dayName = when (scheduleDayOfWeek) {
                        Calendar.MONDAY -> "Senin"
                        Calendar.TUESDAY -> "Selasa"
                        Calendar.WEDNESDAY -> "Rabu"
                        Calendar.THURSDAY -> "Kamis"
                        Calendar.FRIDAY -> "Jumat"
                        Calendar.SATURDAY -> "Sabtu"
                        Calendar.SUNDAY -> "Minggu"
                        else -> "?"
                    }

                    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(waktuMulai)
                    val title = "Jadwal Mendatang: $matkul"
                    val message = "Anda memiliki jadwal kuliah $matkul di $ruangan pada hari $dayName jam $formattedTime."

                    Notification(
                        id = "schedule_${scheduleId}_upcoming",
                        title = title,
                        message = message,
                        timestamp = Date(),
                        type = NotificationType.SCHEDULE,
                        relatedItemId = scheduleId,
                        relatedItemTitle = matkul,
                        isRead = false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing upcoming schedule: ${e.message}")
                    null
                }
            }
            notifications.addAll(upcomingSchedules)

            // BAGIAN 4: TAMBAHKAN NOTIFIKASI SISTEM
            return@withContext notifications
        } catch (e: Exception) {
            Log.e(TAG, "Error generating notifications: ${e.message}")
            return@withContext emptyList()
        }
    }

    private fun isUpcoming(date: Date): Boolean {
        val now = Date()
        val diff = date.time - now.time
        val hours = diff / (60 * 60 * 1000)
        return hours in 0..24
    }

    /**
     * Tambahkan notifikasi baru
     */
    suspend fun addNotification(notification: Notification): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false

            val notificationData = hashMapOf(
                "title" to notification.title,
                "message" to notification.message,
                "timestamp" to notification.timestamp,
                "type" to notification.type.name,
                "relatedItemId" to notification.relatedItemId,
                "relatedItemTitle" to notification.relatedItemTitle,
                "isRead" to notification.isRead
            )

            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notification.id)
                .set(notificationData)
                .await()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding notification: ${e.message}")
            false
        }
    }

    /**
     * Tandai notifikasi sebagai telah dibaca
     */
    suspend fun markNotificationAsRead(notificationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false

            val docRef = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)

            val doc = docRef.get().await()

            if (doc.exists()) {
                docRef.update("isRead", true).await()
            } else if (notificationId.startsWith("task_") || notificationId.startsWith("schedule_")) {
                val persistentNotificationData = hashMapOf(
                    "title" to "Notification",
                    "message" to "This notification has been read",
                    "timestamp" to Date(),
                    "type" to if (notificationId.startsWith("task_")) "TASK" else "SCHEDULE",
                    "relatedItemId" to notificationId.substringAfter("_").substringBefore("_"),
                    "relatedItemTitle" to "",
                    "isRead" to true
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notificationId)
                    .set(persistentNotificationData)
                    .await()
            }

            val currentList = _notificationFlow.value
            val updatedList = currentList.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            _notificationFlow.value = updatedList

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: ${e.message}")
            false
        }
    }

    /**
     * Hapus notifikasi
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

            val currentList = _notificationFlow.value
            val updatedList = currentList.filter { it.id != notificationId }
            _notificationFlow.value = updatedList

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification: ${e.message}")
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

            val readNotifications = _notificationFlow.value.filter { it.isRead }

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

            val currentList = _notificationFlow.value
            val updatedList = currentList.filter { !it.isRead }
            _notificationFlow.value = updatedList

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing read notifications: ${e.message}")
            false
        }
    }

    /**
     * Tandai semua notifikasi sebagai sudah dibaca
     */
    suspend fun markAllNotificationsAsRead(notificationIds: List<String>): Boolean = withContext(Dispatchers.IO) {
        if (notificationIds.isEmpty()) return@withContext true

        try {
            val userId = auth.currentUser?.uid ?: return@withContext false

            val batch = firestore.batch()
            val existingNotificationsRef = mutableListOf<Pair<String, com.google.firebase.firestore.DocumentReference>>()

            for (notificationId in notificationIds) {
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notificationId)

                try {
                    val doc = docRef.get().await()
                    if (doc.exists()) {
                        batch.update(docRef, "isRead", true)
                        existingNotificationsRef.add(notificationId to docRef)
                    } else if (notificationId.startsWith(SCHEDULE_PREFIX) || notificationId.startsWith(TASK_PREFIX)) {
                        val persistentNotificationData = hashMapOf(
                            "title" to "Notification",
                            "message" to "This notification has been marked as read",
                            "timestamp" to Date(),
                            "type" to if (notificationId.startsWith(TASK_PREFIX)) "TASK" else "SCHEDULE",
                            "relatedItemId" to notificationId.substringAfter("_").substringBefore("_"),
                            "relatedItemTitle" to "",
                            "isRead" to true
                        )
                        batch.set(docRef, persistentNotificationData)
                        existingNotificationsRef.add(notificationId to docRef)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking notification ${notificationId}: ${e.message}")
                }
            }

            if (existingNotificationsRef.isNotEmpty()) {
                batch.commit().await()

                val currentList = _notificationFlow.value
                val updatedList = currentList.map {
                    if (it.id in notificationIds) it.copy(isRead = true) else it
                }
                _notificationFlow.value = updatedList

                Log.d(TAG, "Marked ${existingNotificationsRef.size} notifications as read")
                return@withContext true
            }

            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all notifications as read: ${e.message}")
            return@withContext false
        }
    }
}