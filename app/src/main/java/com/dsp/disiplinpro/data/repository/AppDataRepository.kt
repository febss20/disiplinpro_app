package com.dsp.disiplinpro.data.repository

import android.content.Context
import android.util.Log
import com.dsp.disiplinpro.data.model.Notification
import com.dsp.disiplinpro.data.model.Schedule
import com.dsp.disiplinpro.data.model.Task
import com.dsp.disiplinpro.data.model.User
import com.dsp.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale("id"))
    private val dateTimeFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id"))

    @Suppress("unused")
    suspend fun getCompletedTasks(): List<Task> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .whereEqualTo("completed", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("unused")
    suspend fun getIncompleteTasks(): List<Task> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .whereEqualTo("completed", false)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllTasks(): List<Task> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserSchedules(): List<Schedule> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("schedules")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Schedule::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserNotifications(): List<Notification> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserProfile(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSubjects(): Set<String> {
        val tasks = getAllTasks()
        val schedules = getUserSchedules()
        val subjects = mutableSetOf<String>()

        tasks.forEach { task ->
            if (task.matkul.isNotEmpty()) {
                subjects.add(task.matkul)
            }
        }

        schedules.forEach { schedule ->
            if (schedule.matkul.isNotEmpty()) {
                subjects.add(schedule.matkul)
            }
        }

        return subjects
    }

    suspend fun getSecuritySettings(context: Context?): Map<String, Boolean> {
        if (context == null) return emptyMap()

        val securityPrefs = SecurityPrivacyPreferences(context)

        return try {
            mapOf(
                "biometricLoginEnabled" to securityPrefs.biometricLoginFlow.first(),
                "twoFactorAuthEnabled" to securityPrefs.twoFactorAuthFlow.first(),
                "saveLoginInfoEnabled" to securityPrefs.saveLoginInfoFlow.first(),
                "notificationsEnabled" to securityPrefs.allowNotificationsFlow.first(),
                "shareActivityDataEnabled" to securityPrefs.shareActivityDataFlow.first()
            )
        } catch (e: Exception) {
            Log.e("AppDataRepository", "Error getting security settings: ${e.message}")
            emptyMap()
        }
    }

    suspend fun getContextForGemini(context: Context? = null): String {
        val tasks = getAllTasks()
        val user = getUserProfile()
        val schedules = getUserSchedules()
        val notifications = getUserNotifications()
        val subjects = getSubjects()
        val securitySettings = if (context != null) getSecuritySettings(context) else emptyMap()
        val today = Calendar.getInstance()
        val overdueTasksCount = tasks.count { task ->
            !task.completed.isTrue() && task.tanggal.toDate().before(today.time)
        }

        val dueTodayTasksCount = tasks.count { task ->
            !task.completed.isTrue() && isSameDay(task.tanggal.toDate(), today.time)
        }

        val dueTomorrowTasksCount = tasks.count { task ->
            !task.completed.isTrue() && isTomorrow(task.tanggal.toDate(), today.time)
        }

        val dueThisWeekTasksCount = tasks.count { task ->
            !task.completed.isTrue() &&
                    !task.tanggal.toDate().before(today.time) &&
                    isInSameWeek(task.tanggal.toDate(), today.time)
        }

        return buildString {
            append("Informasi Pengguna:\n")
            if (user != null) {
                append("Username: ${user.username}\n")
                append("Email: ${user.email}\n")
                append("Terakhir login: ${if (user.lastLogin > 0) dateFormatter.format(Date(user.lastLogin)) else "Tidak diketahui"}\n")
                append("Metode login: ${if (user.isGoogleUser) "Google" else "Email/Password"}\n")
                if (!user.fotoProfil.isNullOrEmpty()) {
                    append("Memiliki foto profil: Ya\n")
                }
            } else {
                append("Informasi profil tidak tersedia\n")
            }

            if (securitySettings.isNotEmpty()) {
                append("\nPengaturan Keamanan & Privasi:\n")
                append("Login biometrik: ${if (securitySettings["biometricLoginEnabled"] == true) "Aktif" else "Tidak aktif"}\n")
                append("Autentikasi dua faktor: ${if (securitySettings["twoFactorAuthEnabled"] == true) "Aktif" else "Tidak aktif"}\n")
                append("Simpan info login: ${if (securitySettings["saveLoginInfoEnabled"] == true) "Aktif" else "Tidak aktif"}\n")
                append("Notifikasi: ${if (securitySettings["notificationsEnabled"] == true) "Diizinkan" else "Tidak diizinkan"}\n")
                append("Berbagi data aktivitas: ${if (securitySettings["shareActivityDataEnabled"] == true) "Diizinkan" else "Tidak diizinkan"}\n")
            }

            append("\nDaftar Mata Kuliah:\n")
            if (subjects.isNotEmpty()) {
                subjects.forEach { subject ->
                    append("- $subject\n")
                }
            } else {
                append("Belum ada mata kuliah yang tersimpan\n")
            }

            append("\nRingkasan Tugas:\n")
            append("Tugas terlambat: $overdueTasksCount\n")
            append("Tugas hari ini: $dueTodayTasksCount\n")
            append("Tugas besok: $dueTomorrowTasksCount\n")
            append("Tugas minggu ini: $dueThisWeekTasksCount\n")
            append("Total tugas belum selesai: ${tasks.count { !it.completed.isTrue() }}\n")
            append("Total tugas selesai: ${tasks.count { it.completed.isTrue() }}\n")
            append("\nJadwal Kuliah:\n")

            if (schedules.isNotEmpty()) {
                val sortedSchedules = schedules.sortedWith(compareBy({ getDayOrder(it.hari) }, { it.waktuMulai.seconds }))
                sortedSchedules.forEach { schedule ->
                    val waktuMulai = timeFormatter.format(schedule.waktuMulai.toDate())
                    val waktuSelesai = timeFormatter.format(schedule.waktuSelesai.toDate())
                    append("- ${schedule.hari}, $waktuMulai-$waktuSelesai: ${schedule.matkul} (Ruangan: ${schedule.ruangan})\n")
                }
            } else {
                append("Belum ada jadwal kuliah yang tersimpan\n")
            }

            if (overdueTasksCount > 0) {
                append("\nTugas Terlambat:\n")
                val overdueTasks = tasks.filter {
                    !it.completed.isTrue() && it.tanggal.toDate().before(today.time)
                }.sortedBy { it.tanggal.seconds }

                overdueTasks.forEach { task ->
                    val tanggal = dateFormatter.format(task.tanggal.toDate())
                    val waktu = timeFormatter.format(task.waktu.toDate())
                    val terlambat = calculateDaysLate(task.tanggal.toDate(), today.time)

                    append("- ${task.judulTugas} (${task.matkul})\n")
                    append("  Deadline: $tanggal, $waktu (terlambat $terlambat hari)\n")
                }
            }

            if (dueTodayTasksCount > 0) {
                append("\nTugas Hari Ini:\n")
                val todayTasks = tasks.filter {
                    !it.completed.isTrue() && isSameDay(it.tanggal.toDate(), today.time)
                }.sortedBy { it.waktu.seconds }

                todayTasks.forEach { task ->
                    val waktu = timeFormatter.format(task.waktu.toDate())

                    append("- ${task.judulTugas} (${task.matkul})\n")
                    append("  Deadline: Hari ini pukul $waktu\n")
                }
            }

            append("\nTugas Mendatang:\n")
            val upcomingTasks = tasks.filter {
                !it.completed.isTrue() &&
                        !it.tanggal.toDate().before(today.time) &&
                        !isSameDay(it.tanggal.toDate(), today.time)
            }.sortedBy { it.tanggal.seconds }

            if (upcomingTasks.isNotEmpty()) {
                upcomingTasks.forEach { task ->
                    val tanggal = dateFormatter.format(task.tanggal.toDate())
                    val waktu = timeFormatter.format(task.waktu.toDate())
                    val deadline = getRelativeDeadline(task.tanggal.toDate(), today.time)

                    append("- ${task.judulTugas} (${task.matkul})\n")
                    append("  Deadline: $tanggal, $waktu ($deadline)\n")
                }
            } else {
                append("Tidak ada tugas mendatang\n")
            }

            append("\nTugas yang Sudah Selesai:\n")
            val completedTasks = tasks.filter { it.completed.isTrue() }
            if (completedTasks.isNotEmpty()) {
                val recentCompletedTasks = completedTasks.sortedByDescending { it.tanggal.seconds }.take(5)
                recentCompletedTasks.forEach { task ->
                    val tanggal = dateFormatter.format(task.tanggal.toDate())
                    append("- ${task.judulTugas} (${task.matkul}), deadline: $tanggal\n")
                }

                if (completedTasks.size > 5) {
                    append("... dan ${completedTasks.size - 5} tugas lainnya\n")
                }
            } else {
                append("Tidak ada tugas yang sudah selesai\n")
            }

            append("\nNotifikasi Terbaru:\n")
            if (notifications.isNotEmpty()) {
                val recentNotifications = notifications.sortedByDescending { it.timestamp }.take(5)
                recentNotifications.forEach { notification ->
                    val timeAgo = getTimeAgo(notification.timestamp)
                    append("- ${notification.title}: ${notification.message} ($timeAgo)\n")
                }

                if (notifications.size > 5) {
                    append("... dan ${notifications.size - 5} notifikasi lainnya\n")
                }
            } else {
                append("Tidak ada notifikasi\n")
            }

            append("\nStatistik Singkat:\n")
            append("Total mata kuliah: ${subjects.size}\n")
            append("Total jadwal kuliah: ${schedules.size}\n")
            append("Total tugas: ${tasks.size}\n")
            append("Tugas selesai: ${completedTasks.size}\n")
            append("Tugas belum selesai: ${tasks.size - completedTasks.size}\n")
            append("Rasio penyelesaian tugas: ${if (tasks.isNotEmpty()) String.format("%.1f%%", completedTasks.size.toFloat() / tasks.size * 100) else "0%"}\n")
        }
    }

    private fun getDayOrder(day: String): Int {
        return when (day.lowercase()) {
            "senin" -> 1
            "selasa" -> 2
            "rabu" -> 3
            "kamis" -> 4
            "jumat" -> 5
            "sabtu" -> 6
            "minggu" -> 7
            else -> 8
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isTomorrow(date: Date, today: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date }
        val cal2 = Calendar.getInstance().apply { time = today }
        cal2.add(Calendar.DAY_OF_YEAR, 1)
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isInSameWeek(date: Date, today: Date): Boolean {
        val cal1 = Calendar.getInstance().apply {
            time = date
            firstDayOfWeek = Calendar.MONDAY
        }
        val cal2 = Calendar.getInstance().apply {
            time = today
            firstDayOfWeek = Calendar.MONDAY
        }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun calculateDaysLate(deadline: Date, today: Date): Int {
        val diff = today.time - deadline.time
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun getRelativeDeadline(deadline: Date, today: Date): String {
        if (isTomorrow(deadline, today)) return "besok"

        val diffInMillis = deadline.time - today.time
        val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

        return when {
            diffInDays < 7 -> "$diffInDays hari lagi"
            diffInDays < 14 -> "1 minggu lagi"
            diffInDays < 30 -> "${diffInDays / 7} minggu lagi"
            diffInDays < 60 -> "1 bulan lagi"
            else -> "${diffInDays / 30} bulan lagi"
        }
    }

    private fun getTimeAgo(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "baru saja"
            diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)} menit yang lalu"
            diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)} jam yang lalu"
            diff < TimeUnit.DAYS.toMillis(2) -> "kemarin"
            diff < TimeUnit.DAYS.toMillis(7) -> "${diff / TimeUnit.DAYS.toMillis(1)} hari yang lalu"
            else -> dateFormatter.format(date)
        }
    }

    private fun Boolean?.isTrue(): Boolean {
        return this == true
    }
}