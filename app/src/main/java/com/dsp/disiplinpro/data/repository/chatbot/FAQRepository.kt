package com.dsp.disiplinpro.data.repository.chatbot

import com.dsp.disiplinpro.viewmodel.profile.FAQCategory
import com.dsp.disiplinpro.viewmodel.profile.FAQItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FAQRepository @Inject constructor() {
    fun getFAQData(): List<FAQItem> {
        return listOf(
            FAQItem(
                id = 1,
                question = "Bagaimana cara mengubah password akun saya?",
                answer = "Untuk mengubah password, ikuti langkah berikut:\n\n1. Buka halaman Profil\n2. Pilih 'Edit Akun'\n3. Masukkan password lama Anda\n4. Masukkan password baru yang diinginkan\n5. Tekan 'Simpan Perubahan'",
                category = FAQCategory.ACCOUNT
            ),
            FAQItem(
                id = 2,
                question = "Apakah saya bisa mengubah nama pengguna?",
                answer = "Ya, Anda dapat mengubah nama pengguna kapan saja melalui halaman 'Edit Akun' di profil Anda. Perubahan akan langsung terlihat setelah disimpan.",
                category = FAQCategory.ACCOUNT
            ),
            FAQItem(
                id = 3,
                question = "Bagaimana cara logout dari aplikasi?",
                answer = "Untuk logout, buka halaman Profil dan gulir ke bawah. Anda akan menemukan tombol 'Logout' di bagian bawah halaman.",
                category = FAQCategory.ACCOUNT
            ),
            FAQItem(
                id = 4,
                question = "Bagaimana cara menambahkan tugas baru?",
                answer = "Untuk menambahkan tugas baru:\n\n1. Klik tombol '+' pada halaman utama\n2. Isi informasi tugas seperti judul, mata kuliah, tanggal, dll\n3. Tekan 'Simpan' untuk menyimpan tugas baru",
                category = FAQCategory.TASK
            ),
            FAQItem(
                id = 5,
                question = "Bagaimana cara menandai tugas sudah selesai?",
                answer = "Cukup klik kotak centang di samping tugas untuk menandainya sebagai selesai. Anda juga dapat membatalkan tanda ini dengan mengklik kembali kotak tersebut.",
                category = FAQCategory.TASK
            ),
            FAQItem(
                id = 6,
                question = "Apakah saya bisa menghapus tugas yang sudah dibuat?",
                answer = "Ya, untuk menghapus tugas, cukup tekan lama pada tugas yang ingin dihapus, kemudian pilih opsi 'Hapus' yang muncul.",
                category = FAQCategory.TASK
            ),
            FAQItem(
                id = 7,
                question = "Bagaimana cara menggunakan fitur AI Asisten?",
                answer = "AI Asisten dapat diakses melalui tombol bulat yang dapat digeser di layar. Klik tombol tersebut untuk membuka chatbot dan tanyakan bantuan tentang penggunaan aplikasi, tugas, atau jadwal Anda.",
                category = FAQCategory.OTHER
            ),
            FAQItem(
                id = 8,
                question = "Apakah notifikasi dapat disesuaikan?",
                answer = "Ya, Anda dapat menyesuaikan notifikasi di halaman 'Keamanan dan Privasi'. Anda dapat mengaktifkan atau menonaktifkan notifikasi dan mengatur jenis notifikasi yang ingin diterima.",
                category = FAQCategory.OTHER
            ),
            FAQItem(
                id = 9,
                question = "Bagaimana cara melaporkan bug atau masalah aplikasi?",
                answer = "Untuk melaporkan bug atau masalah, silakan kirim email ke support@disiplinpro.id dengan detail masalah yang Anda alami. Tim kami akan merespons secepat mungkin.",
                category = FAQCategory.OTHER
            ),
            FAQItem(
                id = 10,
                question = "Di mana saya bisa mendapatkan bantuan lebih lanjut?",
                answer = "Anda dapat menghubungi tim dukungan kami melalui email support@disiplinpro.id atau kunjungi situs web kami di www.disiplinpro.id untuk informasi bantuan lebih lanjut.",
                category = FAQCategory.OTHER
            ),
            FAQItem(
                id = 11,
                question = "Apa itu DisiplinPro?",
                answer = "DisiplinPro adalah aplikasi manajemen tugas dan jadwal yang dirancang khusus untuk mahasiswa. Aplikasi ini membantu Anda mengatur tugas kuliah, jadwal, dan kegiatan akademik untuk meningkatkan produktivitas dan kedisiplinan.",
                category = FAQCategory.OTHER
            ),
            FAQItem(
                id = 12,
                question = "Bagaimana cara menggunakan fitur pengingat?",
                answer = "Fitur pengingat akan otomatis aktif untuk tugas yang Anda buat. Anda akan menerima notifikasi sebelum deadline tugas tiba. Anda dapat mengatur preferensi notifikasi di menu Pengaturan > Keamanan dan Privasi.",
                category = FAQCategory.TASK
            ),
            FAQItem(
                id = 13,
                question = "Bagaimana cara menambahkan jadwal baru?",
                answer = "Untuk menambahkan jadwal baru:\n\n1. Pergi ke halaman Jadwal\n2. Tekan tombol '+' di sudut kanan bawah\n3. Isi informasi jadwal seperti mata kuliah, hari, waktu, dan ruangan\n4. Tekan 'Simpan' untuk menyimpan jadwal",
                category = FAQCategory.TASK
            )
        )
    }

    fun getFAQByCategory(category: FAQCategory): List<FAQItem> {
        return getFAQData().filter { it.category == category }
    }

    fun getFAQById(id: Int): FAQItem? {
        return getFAQData().find { it.id == id }
    }

    fun searchFAQ(query: String): List<FAQItem> {
        val lowercaseQuery = query.lowercase()
        return getFAQData().filter {
            it.question.lowercase().contains(lowercaseQuery) ||
                    it.answer.lowercase().contains(lowercaseQuery)
        }
    }
}