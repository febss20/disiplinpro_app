package com.dsp.disiplinpro.data.model

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
    val relatedItemId: String,
    val relatedItemTitle: String,
    val isRead: Boolean = false
)

/**
 * Jenis notifikasi yang didukung aplikasi
 */
enum class NotificationType {
    TASK,
    SCHEDULE,
    SYSTEM
}

/**
 * Status tambahan untuk notifikasi
 */
enum class NotificationStatus {
    UPCOMING,
    ACTIVE,
    PAST
}