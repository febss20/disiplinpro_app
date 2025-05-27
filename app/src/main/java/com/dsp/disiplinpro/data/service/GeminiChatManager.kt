package com.dsp.disiplinpro.data.service

import android.content.Context
import com.dsp.disiplinpro.data.repository.chatbot.AppDataRepository
import com.dsp.disiplinpro.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiChatManager @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val context: Context
) {
    private val generativeModel by lazy {
        val apiKey = BuildConfig.GEMINI_API_KEY
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey
        )
    }

    private val chatHistory = mutableListOf<Pair<String, String>>()

    suspend fun getChatResponse(userQuery: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val appContext = appDataRepository.getContextForGemini(context)
                chatHistory.add(Pair("user", userQuery))
                val prompt = buildPrompt(appContext, userQuery)
                val response = sendPromptToGemini(prompt)
                val aiResponse = response.text ?: ""
                if (aiResponse.isNotEmpty()) {
                    chatHistory.add(Pair("ai", aiResponse))
                }

                aiResponse
            } catch (e: Exception) {
                "Maaf, terjadi kesalahan saat berkomunikasi dengan AI: ${e.message}"
            }
        }
    }

    private suspend fun sendPromptToGemini(prompt: String): GenerateContentResponse {
        val content = content {
            text(prompt)
        }

        return generativeModel.generateContent(content)
    }

    private fun buildPrompt(appContext: String, userQuery: String): String {
        val username = extractUsername(appContext)

        val relevantContextSections = getRelevantContextSections(appContext, userQuery)

        val queryType = categorizeQuery(userQuery)

        return """
            Kamu adalah asisten AI dalam aplikasi DisiplinPro, sebuah aplikasi manajemen tugas dan disiplin untuk mahasiswa.
            
            # IDENTITAS DAN PERAN
            - Namamu adalah "DisiplinPro Assistant"
            - Kamu adalah asisten pintar yang membantu pengguna mengelola tugas, jadwal, dan aktivitas akademik
            - Kamu selalu menjawab dengan santai, menggunakan bahasa gaul yang digunakan anak muda dan mahasiswa zaman sekarang
            
            # DATA KONTEKS PENGGUNA
            ${if (username.isNotEmpty()) "Pengguna saat ini: $username" else ""}
            
            # INFORMASI APLIKASI YANG RELEVAN
            $relevantContextSections
            
            # RIWAYAT PERCAKAPAN
            ${formatChatHistory()}
            
            # PERTANYAAN PENGGUNA
            $userQuery
            
            # INSTRUKSI KHUSUS
            ${getSpecialInstructions(queryType)}
            
            # TATA CARA MENJAWAB
            - Jawab secara langsung dan to the point
            - Gunakan bahasa gaul Indonesia yang populer di kalangan mahasiswa (bisa pakai 'gue/lu', 'aku/kamu', singkatan populer, dll)
            - Sisipkan slang/jargon kekinian yang relevan dan mudah dipahami
            - Tetap informatif tapi dengan gaya santai, seperti chatting dengan teman
            - Gunakan emoji sesekali untuk ekspresi 😉
            - Bisa pakai singkatan populer (kyk, bgt, sih, dong, deh, btw, yg, dll)
            - Bersikap helpful tapi dengan gaya chill, tidak terlalu formal
            - Jika informasi tidak tersedia, bilang aja dengan jujur dengan gaya santai
            - Personalisasi jawaban dengan menyebut nama pengguna kalau ada
            - Hindari bahasa yang terlalu formal atau kaku
            
            # BATASAN
            - Kamu tidak dapat membuat, mengubah, atau menghapus data di aplikasi
            - Kamu tidak dapat melakukan tindakan di luar memberikan informasi dan saran
            - Jika diminta melakukan tindakan yang tidak memungkinkan, arahkan pengguna ke antarmuka aplikasi yang sesuai
        """.trimIndent()
    }

    private fun extractUsername(appContext: String): String {
        val usernameRegex = "Username: ([^\\n]+)".toRegex()
        val matchResult = usernameRegex.find(appContext)
        return matchResult?.groupValues?.getOrNull(1) ?: ""
    }

    private fun getRelevantContextSections(appContext: String, userQuery: String): String {
        val lowercaseQuery = userQuery.lowercase()

        val sections = mapOf(
            "tugas|deadline|pr|assignment" to "Ringkasan Tugas:|Tugas Terlambat:|Tugas Hari Ini:|Tugas Mendatang:|Tugas yang Sudah Selesai:",
            "jadwal|kuliah|kelas|mata kuliah|matkul" to "Jadwal Kuliah:|Jadwal Kuliah Hari Ini:|Detail Mata Kuliah:",
            "notifikasi|pemberitahuan|pengingat" to "Pengaturan Keamanan & Privasi:|Notifikasi:",
            "akun|profil|user|pengguna|login" to "Informasi Pengguna:|Pengaturan Keamanan & Privasi:",
            "bantuan|help|faq" to "Informasi FAQ dan Bantuan:|Bantuan Akun:|Bantuan Tugas dan Jadwal:|Bantuan Umum:",
            "mata kuliah|subjek|matkul" to "Daftar Mata Kuliah:|Detail Mata Kuliah:|Statistik Mata Kuliah:",
            "developer|pembuat|pengembang|kreator|versi|tentang|about|kontak|tim|contact" to "Tentang Pembuat Aplikasi:|Tentang DisiplinPro:"
        )

        val relevantRegexes = sections.entries.filter { (keywords, _) ->
            keywords.split("|").any { keyword -> lowercaseQuery.contains(keyword) }
        }.flatMap { it.value.split("|") }

        if (lowercaseQuery.contains("developer") ||
            lowercaseQuery.contains("pembuat") ||
            lowercaseQuery.contains("pengembang") ||
            lowercaseQuery.contains("kreator") ||
            lowercaseQuery.contains("siapa yang membuat")) {

            return extractSection(appContext, "Tentang Pembuat Aplikasi:", "Bantuan Akun:")
        }

        if (relevantRegexes.isEmpty()) {
            return """
                # Ringkasan Data Utama
                ${extractSection(appContext, "Informasi Pengguna:", "Daftar Mata Kuliah:")}
                ${extractSection(appContext, "Ringkasan Tugas:", "Jadwal Kuliah:")}
                
                # Informasi Jadwal
                ${extractSection(appContext, "Jadwal Kuliah Hari Ini:", "Tugas Terlambat:")}
            """.trimIndent()
        }

        return buildString {
            relevantRegexes.forEach { sectionMarker ->
                val sectionContent = extractSection(appContext, sectionMarker)
                if (sectionContent.isNotEmpty()) {
                    append("# $sectionMarker\n")
                    append(sectionContent)
                    append("\n\n")
                }
            }
        }
    }

    private fun extractSection(appContext: String, sectionMarker: String, endMarker: String? = null): String {
        val startIndex = appContext.indexOf(sectionMarker)
        if (startIndex == -1) return ""

        val endIndex = if (endMarker != null) {
            val end = appContext.indexOf(endMarker, startIndex)
            if (end == -1) appContext.length else end
        } else {
            val nextSectionIndex = appContext.indexOf("\n\n", startIndex + sectionMarker.length)
            if (nextSectionIndex == -1) appContext.length else nextSectionIndex
        }

        return appContext.substring(startIndex, endIndex).trim()
    }

    private fun categorizeQuery(query: String): String {
        val lowercaseQuery = query.lowercase()

        return when {
            lowercaseQuery.contains("tugas") && (lowercaseQuery.contains("tambah") || lowercaseQuery.contains("buat")) -> "CREATE_TASK"
            lowercaseQuery.contains("jadwal") && (lowercaseQuery.contains("tambah") || lowercaseQuery.contains("buat")) -> "CREATE_SCHEDULE"
            lowercaseQuery.contains("tugas") && lowercaseQuery.contains("ubah") -> "EDIT_TASK"
            lowercaseQuery.contains("jadwal") && lowercaseQuery.contains("ubah") -> "EDIT_SCHEDULE"
            lowercaseQuery.contains("tugas") && lowercaseQuery.contains("hapus") -> "DELETE_TASK"
            lowercaseQuery.contains("jadwal") && lowercaseQuery.contains("hapus") -> "DELETE_SCHEDULE"
            lowercaseQuery.contains("deadline") || lowercaseQuery.contains("tugas") -> "TASK_INFO"
            lowercaseQuery.contains("jadwal") || lowercaseQuery.contains("kuliah") -> "SCHEDULE_INFO"
            lowercaseQuery.contains("notifikasi") || lowercaseQuery.contains("pengingat") -> "NOTIFICATION_INFO"
            lowercaseQuery.contains("bantuan") || lowercaseQuery.contains("cara") -> "HELP"
            lowercaseQuery.contains("profil") || lowercaseQuery.contains("akun") -> "PROFILE_INFO"
            else -> "GENERAL"
        }
    }

    private fun getSpecialInstructions(queryType: String): String {
        return when (queryType) {
            "CREATE_TASK", "EDIT_TASK", "DELETE_TASK" ->
                "Pengguna ingin melakukan perubahan pada tugas. Beri tahu bahwa kamu tidak dapat langsung membuat/mengubah/menghapus data. " +
                        "Jelaskan cara melakukannya melalui antarmuka aplikasi: Tekan tombol + di halaman Tugas, lalu isi detail yang diperlukan."

            "CREATE_SCHEDULE", "EDIT_SCHEDULE", "DELETE_SCHEDULE" ->
                "Pengguna ingin melakukan perubahan pada jadwal. Beri tahu bahwa kamu tidak dapat langsung membuat/mengubah/menghapus data. " +
                        "Jelaskan cara melakukannya melalui antarmuka aplikasi: Buka halaman Jadwal, tekan tombol + untuk menambah atau tekan lama pada jadwal untuk mengedit/menghapus."

            "TASK_INFO" ->
                "Pengguna bertanya tentang informasi tugas. Berikan informasi detail dan terorganisir tentang tugas yang ada dalam data."

            "SCHEDULE_INFO" ->
                "Pengguna bertanya tentang jadwal. Berikan informasi detail dan terorganisir tentang jadwal yang ada dalam data."

            "NOTIFICATION_INFO" ->
                "Pengguna bertanya tentang notifikasi. Berikan informasi detail tentang pengaturan notifikasi, cara kerjanya, dan fitur terkait."

            "HELP" ->
                "Pengguna meminta bantuan. Berikan panduan langkah demi langkah yang jelas dan mudah diikuti."

            "PROFILE_INFO" ->
                "Pengguna bertanya tentang informasi profil/akun. Berikan informasi yang tersedia dan cara mengubahnya jika diperlukan."

            "DEVELOPER_INFO" ->
                "Pengguna bertanya tentang informasi pembuat aplikasi. Berikan informasi yang tersedia."

            else ->
                "Berikan jawaban yang relevan dan berdasarkan data yang tersedia. Jika informasi tidak ada dalam data, sampaikan dengan jujur."
        }
    }

    private fun formatChatHistory(): String {
        val recentHistory = chatHistory.takeLast(10)

        if (recentHistory.isEmpty()) {
            return "Tidak ada riwayat percakapan sebelumnya."
        }

        return buildString {
            recentHistory.forEach { (role, message) ->
                append("${if (role == "user") "Pengguna" else "AI"}: $message\n")
            }
        }
    }

    fun clearChatHistory() {
        chatHistory.clear()
    }
}