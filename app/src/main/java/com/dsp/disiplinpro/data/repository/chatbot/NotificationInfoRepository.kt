package com.dsp.disiplinpro.data.repository.chatbot

import com.dsp.disiplinpro.data.model.NotificationType
import com.dsp.disiplinpro.data.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationInfoRepository @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend fun getNotificationDetailInfo(): String {
        return try {
            val notifications = notificationRepository.getAllNotifications()

            buildString {
                append("\nInformasi Detail Notifikasi:\n")

                if (notifications.isNotEmpty()) {
                    val byType = notifications.groupBy { it.type }

                    append("Total notifikasi tergenerate: ${notifications.size}\n")
                    append("Notifikasi tugas: ${byType[NotificationType.TASK]?.size ?: 0}\n")
                    append("Notifikasi jadwal: ${byType[NotificationType.SCHEDULE]?.size ?: 0}\n")
                    append("Notifikasi sistem: ${byType[NotificationType.SYSTEM]?.size ?: 0}\n")
                    append("Notifikasi belum dibaca: ${notifications.count { !it.isRead }}\n\n")

                    append("Detail Notifikasi Tergenerate:\n")

                    val taskNotifications = notifications.filter { it.type == NotificationType.TASK }
                    if (taskNotifications.isNotEmpty()) {
                        append("Notifikasi Tugas:\n")
                        taskNotifications.take(3).forEach { notification ->
                            append("- ${notification.title}: ${notification.message}\n")
                        }
                        if (taskNotifications.size > 3) {
                            append("  ... dan ${taskNotifications.size - 3} notifikasi tugas lainnya\n")
                        }
                        append("\n")
                    }

                    val scheduleNotifications = notifications.filter { it.type == NotificationType.SCHEDULE }
                    if (scheduleNotifications.isNotEmpty()) {
                        append("Notifikasi Jadwal:\n")
                        scheduleNotifications.take(3).forEach { notification ->
                            append("- ${notification.title}: ${notification.message}\n")
                        }
                        if (scheduleNotifications.size > 3) {
                            append("  ... dan ${scheduleNotifications.size - 3} notifikasi jadwal lainnya\n")
                        }
                        append("\n")
                    }
                }

                append("Sistem Notifikasi DisiplinPro:\n")
                append("- Aplikasi menggunakan WorkManager dan AlarmManager untuk penjadwalan notifikasi\n")
                append("- Notifikasi dapat dikonfigurasi di pengaturan Keamanan dan Privasi\n")
                append("- Tipe notifikasi: pengingat tugas, jadwal mendatang, dan notifikasi sistem\n")
                append("- Notifikasi tugas dikirim sebelum tenggat waktu (dapat dikonfigurasi)\n")
                append("- Notifikasi jadwal dikirim sebelum, selama, dan setelah jadwal kuliah\n")
            }
        } catch (e: Exception) {
            "\nTidak dapat mengambil informasi notifikasi tambahan: ${e.message}\n"
        }
    }
}