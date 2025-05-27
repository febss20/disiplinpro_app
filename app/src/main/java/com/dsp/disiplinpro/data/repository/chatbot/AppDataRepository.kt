package com.dsp.disiplinpro.data.repository.chatbot

import android.content.Context
import com.dsp.disiplinpro.util.DateTimeUtils
import com.dsp.disiplinpro.viewmodel.profile.FAQCategory
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataRepository @Inject constructor(
    private val taskRepository: TaskRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val faqRepository: FAQRepository,
    private val subjectRepository: SubjectRepository,
    private val notificationInfoRepository: NotificationInfoRepository
) {

    suspend fun getContextForGemini(context: Context? = null): String {
        val tasks = taskRepository.getAllTasks()
        val user = userRepository.getUserProfile()
        val schedules = scheduleRepository.getUserSchedules()
        val subjects = subjectRepository.getSubjects()
        val securitySettings = if (context != null) userRepository.getSecuritySettings(context) else emptyMap()
        val faqData = faqRepository.getFAQData()

        val todayName = DateTimeUtils.getCurrentDayName()
        val todaySchedules = scheduleRepository.getTodaySchedules(todayName)
        val incompleteTasks = taskRepository.getIncompleteTasks()
        val hasProfilePhoto = userRepository.hasProfilePhoto()
        val loginMethod = userRepository.getLoginMethod()
        val subjectStats = subjectRepository.getSubjectStats()

        val today = Calendar.getInstance()
        val todayDate = today.time

        val overdueTasksCount = taskRepository.getOverdueTasks(todayDate).size
        val dueTodayTasksCount = taskRepository.getTodayTasks(todayDate).size
        val dueTomorrowTasksCount = taskRepository.getTomorrowTasks(todayDate).size
        val dueThisWeekTasksCount = taskRepository.getThisWeekTasks(todayDate).size

        return buildString {
            append("Informasi Pengguna:\n")
            if (user != null) {
                append("Username: ${user.username}\n")
                append("Email: ${user.email}\n")
                append("Terakhir login: ${if (user.lastLogin > 0) DateTimeUtils.formatDate(Date(user.lastLogin)) else "Tidak diketahui"}\n")
                append("Metode login: ${loginMethod ?: (if (user.isGoogleUser) "Google" else "Email/Password")}\n")
                append("Memiliki foto profil: ${if (hasProfilePhoto) "Ya" else "Tidak"}\n")
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
                val sortedSchedules = scheduleRepository.getSortedSchedules()
                sortedSchedules.forEach { schedule ->
                    val waktuMulai = DateTimeUtils.formatTime(schedule.waktuMulai.toDate())
                    val waktuSelesai = DateTimeUtils.formatTime(schedule.waktuSelesai.toDate())
                    append("- ${schedule.hari}, $waktuMulai-$waktuSelesai: ${schedule.matkul} (Ruangan: ${schedule.ruangan})\n")
                }
            } else {
                append("Belum ada jadwal kuliah yang tersimpan\n")
            }

            append("\nJadwal Kuliah Hari Ini:\n")
            if (todaySchedules.isNotEmpty()) {
                todaySchedules.sortedBy { it.waktuMulai.seconds }.forEach { schedule ->
                    val waktuMulai = DateTimeUtils.formatTime(schedule.waktuMulai.toDate())
                    val waktuSelesai = DateTimeUtils.formatTime(schedule.waktuSelesai.toDate())
                    append("- $waktuMulai-$waktuSelesai: ${schedule.matkul} (Ruangan: ${schedule.ruangan})\n")
                }
            } else {
                append("Tidak ada jadwal kuliah hari ini\n")
            }

            if (overdueTasksCount > 0) {
                append("\nTugas Terlambat:\n")
                val overdueTasks = taskRepository.getOverdueTasks(todayDate).sortedBy { it.tanggal.seconds }

                overdueTasks.forEach { task ->
                    val tanggal = DateTimeUtils.formatDate(task.tanggal.toDate())
                    val waktu = DateTimeUtils.formatTime(task.waktu.toDate())
                    val terlambat = DateTimeUtils.calculateDaysLate(task.tanggal.toDate(), todayDate)

                    append("- ${task.judulTugas} (${task.matkul})\n")
                    append("  Deadline: $tanggal, $waktu (terlambat $terlambat hari)\n")
                }
            }

            if (dueTodayTasksCount > 0) {
                append("\nTugas Hari Ini:\n")
                val todayTasks = taskRepository.getTodayTasks(todayDate).sortedBy { it.waktu.seconds }

                todayTasks.forEach { task ->
                    val waktu = DateTimeUtils.formatTime(task.waktu.toDate())

                    append("- ${task.judulTugas} (${task.matkul})\n")
                    append("  Deadline: Hari ini pukul $waktu\n")
                }
            }

            append("\nTugas Mendatang:\n")
            val upcomingTasks = tasks.filter {
                !it.completed.isTrue() &&
                        !it.tanggal.toDate().before(todayDate) &&
                        !DateTimeUtils.isSameDay(it.tanggal.toDate(), todayDate)
            }.sortedBy { it.tanggal.seconds }

            if (upcomingTasks.isNotEmpty()) {
                upcomingTasks.forEach { task ->
                    val tanggal = DateTimeUtils.formatDate(task.tanggal.toDate())
                    val waktu = DateTimeUtils.formatTime(task.waktu.toDate())
                    val deadline = DateTimeUtils.getRelativeDeadline(task.tanggal.toDate(), todayDate)

                    append("- ${task.judulTugas} (${task.matkul})\n")
                    append("  Deadline: $tanggal, $waktu ($deadline)\n")
                }
            } else {
                append("Tidak ada tugas mendatang\n")
            }

            append("\nTugas yang Sudah Selesai:\n")
            val completedTasks = taskRepository.getCompletedTasks()
            if (completedTasks.isNotEmpty()) {
                val recentCompletedTasks = completedTasks.sortedByDescending { it.tanggal.seconds }.take(5)
                recentCompletedTasks.forEach { task ->
                    val tanggal = DateTimeUtils.formatDate(task.tanggal.toDate())
                    append("- ${task.judulTugas} (${task.matkul}), deadline: $tanggal\n")
                }

                if (completedTasks.size > 5) {
                    append("... dan ${completedTasks.size - 5} tugas lainnya\n")
                }
            } else {
                append("Tidak ada tugas yang sudah selesai\n")
            }

            append(notificationInfoRepository.getNotificationDetailInfo())

            append("\nInformasi FAQ dan Bantuan:\n")
            val categorizedFaq = faqData.groupBy { it.category }

            append("\nBantuan Akun:\n")
            categorizedFaq[FAQCategory.ACCOUNT]?.forEach { faq ->
                append("Q: ${faq.question}\n")
                append("A: ${faq.answer}\n\n")
            }

            append("\nBantuan Tugas dan Jadwal:\n")
            categorizedFaq[FAQCategory.TASK]?.forEach { faq ->
                append("Q: ${faq.question}\n")
                append("A: ${faq.answer}\n\n")
            }

            append("\nBantuan Umum:\n")
            categorizedFaq[FAQCategory.OTHER]?.forEach { faq ->
                append("Q: ${faq.question}\n")
                append("A: ${faq.answer}\n\n")
            }

            append("\nTentang DisiplinPro:\n")
            append("DisiplinPro adalah aplikasi manajemen tugas dan jadwal untuk mahasiswa. Aplikasi ini membantu pengguna mengatur tugas kuliah, jadwal perkuliahan, dan kegiatan akademik lainnya. Fitur utama termasuk manajemen tugas, penjadwalan kuliah, notifikasi pengingat, dan AI Assistant untuk membantu pengguna.\n\n")
            append("Versi saat ini dilengkapi dengan fitur AI Assistant yang dapat diakses melalui tombol floating yang dapat digeser-geser di layar aplikasi. Assistant ini dapat membantu pengguna dengan pertanyaan terkait penggunaan aplikasi, manajemen tugas, dan informasi jadwal.\n")

            append("\nStatistik Singkat:\n")
            append("Total mata kuliah: ${subjects.size}\n")
            append("Total jadwal kuliah: ${schedules.size}\n")
            append("Jadwal hari ini: ${todaySchedules.size}\n")
            append("Total tugas: ${tasks.size}\n")
            val completedTasksCount = tasks.size - incompleteTasks.size
            append("Tugas selesai: $completedTasksCount\n")
            append("Tugas belum selesai: ${incompleteTasks.size}\n")
            append("Rasio penyelesaian tugas: ${if (tasks.isNotEmpty()) String.format("%.1f%%", completedTasksCount.toFloat() / tasks.size * 100) else "0%"}\n")

            append("\nStatistik Mata Kuliah:\n")
            if (subjectStats.isNotEmpty()) {
                subjectStats.forEach { (subject, stats) ->
                    append("- $subject:\n")
                    append("  Total tugas: ${stats["totalTasks"] ?: 0}\n")
                    append("  Tugas selesai: ${stats["completedTasks"] ?: 0}\n")
                    append("  Tugas belum selesai: ${stats["incompleteTasks"] ?: 0}\n")
                    append("  Total jadwal: ${stats["scheduleCount"] ?: 0}\n")
                }
            } else {
                append("Tidak ada statistik mata kuliah tersedia\n")
            }

            if (subjects.isNotEmpty()) {
                append("\nDetail Mata Kuliah:\n")
                subjects.take(3).forEach { subject ->
                    append("\n- Mata Kuliah: $subject\n")

                    val tasksBySubject = subjectRepository.getTasksBySubject(subject)
                    if (tasksBySubject.isNotEmpty()) {
                        append("  Tugas ($subject):\n")
                        tasksBySubject.take(3).forEach { task ->
                            val tanggal = DateTimeUtils.formatDate(task.tanggal.toDate())
                            val status = if (task.completed.isTrue()) "Selesai" else "Belum selesai"
                            append("  • ${task.judulTugas} ($tanggal) - $status\n")
                        }
                        if (tasksBySubject.size > 3) {
                            append("  • ... dan ${tasksBySubject.size - 3} tugas lainnya\n")
                        }
                    } else {
                        append("  Tidak ada tugas untuk mata kuliah ini\n")
                    }

                    val schedulesBySubject = subjectRepository.getSchedulesBySubject(subject)
                    if (schedulesBySubject.isNotEmpty()) {
                        append("  Jadwal ($subject):\n")
                        schedulesBySubject.forEach { schedule ->
                            val waktuMulai = DateTimeUtils.formatTime(schedule.waktuMulai.toDate())
                            val waktuSelesai = DateTimeUtils.formatTime(schedule.waktuSelesai.toDate())
                            append("  • ${schedule.hari}, $waktuMulai-$waktuSelesai (${schedule.ruangan})\n")
                        }
                    } else {
                        append("  Tidak ada jadwal untuk mata kuliah ini\n")
                    }
                }

                if (subjects.size > 3) {
                    append("\n... dan ${subjects.size - 3} mata kuliah lainnya\n")
                }
            }

            append("\nTugas Belum Selesai:\n")
            if (incompleteTasks.isNotEmpty()) {
                val sortedIncompleteTasks = incompleteTasks.sortedBy { it.tanggal.seconds }
                sortedIncompleteTasks.take(10).forEach { task ->
                    val tanggal = DateTimeUtils.formatDate(task.tanggal.toDate())
                    val waktu = DateTimeUtils.formatTime(task.waktu.toDate())
                    append("- ${task.judulTugas} (${task.matkul})\n")
                    append("  Deadline: $tanggal, $waktu\n")
                }

                if (incompleteTasks.size > 10) {
                    append("... dan ${incompleteTasks.size - 10} tugas lainnya\n")
                }
            } else {
                append("Tidak ada tugas yang belum selesai\n")
            }

            val developerInfo = userRepository.getAppDeveloperInfo()
            append("\nTentang Pembuat Aplikasi:\n")
            append("${developerInfo["developer"]}\n\n")
            append("Tim Pengembang:\n")
            append("- ${developerInfo["developer_name"]}\n")
            append("Aplikasi ini dibangun sebagai bagian dari proyek mata kuliah Pengembangan Aplikasi Mobile. Versi pertama diluncurkan pada tahun ${developerInfo["launch_year"]} dan terus dikembangkan dengan fitur baru secara berkala.\n\n")
            append("Kontak:\n")
            append("Email: ${developerInfo["email"]}\n")
            append("Website: ${developerInfo["website"]}\n")
            append("GitHub: ${developerInfo["github"]}\n")
            append("Instagram: ${developerInfo["instagram"]}\n")
        }
    }

    private fun Boolean?.isTrue(): Boolean {
        return this == true
    }
}