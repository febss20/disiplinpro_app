package com.example.disiplinpro.data.model

import java.util.Date

/**
 * Model data untuk notifikasi
 */
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Date,
    val type: NotificationType,
    val relatedItemId: String, // ID tugas atau jadwal terkait
    val relatedItemTitle: String, // Judul tugas atau mata kuliah
    val isRead: Boolean = false
)

/**
 * Jenis notifikasi yang didukung aplikasi
 */
enum class NotificationType {
    TASK, // Notifikasi tugas
    SCHEDULE, // Notifikasi jadwal kuliah
    SYSTEM // Notifikasi sistem
}

/**
 * Status tambahan untuk notifikasi
 */
enum class NotificationStatus {
    UPCOMING, // Akan datang
    ACTIVE, // Aktif/sedang berlangsung
    PAST // Sudah lewat
}