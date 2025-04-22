package com.example.disiplinpro.viewmodel.notification

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.model.NotificationHistory
import com.example.disiplinpro.data.model.NotificationType
import com.example.disiplinpro.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationHistoryViewModel : ViewModel() {
    private val repository = NotificationRepository()

    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error = _error

    private val _notifications = MutableStateFlow<List<NotificationHistory>>(emptyList())
    val notifications: StateFlow<List<NotificationHistory>> = _notifications.asStateFlow()

    private val _currentFilter = MutableStateFlow<NotificationType?>(null)
    val currentFilter: StateFlow<NotificationType?> = _currentFilter.asStateFlow()

    init {
        loadNotifications()
    }

    /**
     * Load semua notifikasi
     */
    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val notificationList = repository.getAllNotificationHistory()

                _notifications.value = notificationList
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error loading notifications: ${e.message}")
                _error.value = "Gagal memuat riwayat notifikasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filter notifikasi berdasarkan tipe
     */
    fun setFilter(type: NotificationType?) {
        _currentFilter.value = type
    }

    /**
     * Tandai notifikasi sebagai dibaca
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markNotificationAsRead(notificationId)

                val updatedList = _notifications.value.map {
                    if (it.id == notificationId) {
                        it.copy(isRead = true)
                    } else {
                        it
                    }
                }
                _notifications.value = updatedList
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error marking notification as read: ${e.message}")
                _error.value = "Gagal menandai notifikasi: ${e.message}"
            }
        }
    }

    /**
     * Hapus semua notifikasi yang sudah dibaca
     */
    fun clearReadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                repository.clearReadNotifications()

                val updatedList = _notifications.value.filter { !it.isRead }
                _notifications.value = updatedList
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error clearing read notifications: ${e.message}")
                _error.value = "Gagal menghapus notifikasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Format timestamp menjadi string yang mudah dibaca
     */
    fun formatTimestamp(date: Date): String {
        val today = Date()
        val diffMillis = today.time - date.time
        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMillis / (1000 * 60 * 60)
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)

        return when {
            diffMinutes < 1 -> "Baru saja"
            diffMinutes < 60 -> "$diffMinutes menit yang lalu"
            diffHours < 24 -> "$diffHours jam yang lalu"
            diffHours < 48 -> "Kemarin"
            diffDays < 7 -> "$diffDays hari yang lalu"
            else -> {
                val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                formatter.format(date)
            }
        }
    }

    /**
     * Hapus notifikasi tertentu
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val success = repository.deleteNotification(notificationId)

                if (success) {
                    val updatedList = _notifications.value.filter { it.id != notificationId }
                    _notifications.value = updatedList
                } else {
                    _error.value = "Gagal menghapus notifikasi"
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error deleting notification: ${e.message}")
                _error.value = "Gagal menghapus notifikasi: ${e.message}"
            }
        }
    }
}