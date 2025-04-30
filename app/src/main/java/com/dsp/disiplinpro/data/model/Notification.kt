package com.dsp.disiplinpro.data.model

import java.util.Date

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

enum class NotificationType {
    TASK,
    SCHEDULE,
    SYSTEM
}

enum class NotificationStatus {
    UPCOMING,
    ACTIVE,
    PAST
}