package com.example.disiplinpro.viewmodel.profile

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FAQViewModel(application: Application) : AndroidViewModel(application) {

    // Data untuk FAQ
    private val _faqItems = mutableStateListOf<FAQItem>()
    val faqItems: List<FAQItem> = _faqItems

    // State untuk pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State untuk filter kategori
    private val _selectedCategory = MutableStateFlow(FAQCategory.ALL)
    val selectedCategory: StateFlow<FAQCategory> = _selectedCategory.asStateFlow()

    // State untuk hasil pencarian
    private val _filteredFaqItems = MutableStateFlow<List<FAQItem>>(emptyList())
    val filteredFaqItems: StateFlow<List<FAQItem>> = _filteredFaqItems.asStateFlow()

    // Loading state
    val isLoading = mutableStateOf(false)

    // Init block untuk memuat data FAQ
    init {
        loadFaqItems()
        updateFilteredItems()
    }

    private fun loadFaqItems() {
        // Simulasi loading dari database
        viewModelScope.launch {
            isLoading.value = true
            delay(300) // Simulasi loading delay

            // Data Kategori Akun & Pengaturan
            _faqItems.add(
                FAQItem(
                    id = 1,
                    question = "Bagaimana cara mengubah password akun saya?",
                    answer = "Untuk mengubah password, ikuti langkah berikut:\n\n1. Buka halaman Profil\n2. Pilih 'Edit Akun'\n3. Masukkan password lama Anda\n4. Masukkan password baru yang diinginkan\n5. Tekan 'Simpan Perubahan'",
                    category = FAQCategory.ACCOUNT
                )
            )

            _faqItems.add(
                FAQItem(
                    id = 2,
                    question = "Apakah saya bisa mengubah nama pengguna?",
                    answer = "Ya, Anda dapat mengubah nama pengguna kapan saja melalui halaman 'Edit Akun' di profil Anda. Perubahan akan langsung terlihat setelah disimpan.",
                    category = FAQCategory.ACCOUNT
                )
            )

            _faqItems.add(
                FAQItem(
                    id = 3,
                    question = "Bagaimana cara logout dari aplikasi?",
                    answer = "Untuk logout, buka halaman Profil dan gulir ke bawah. Anda akan menemukan tombol 'Logout' di bagian bawah halaman.",
                    category = FAQCategory.ACCOUNT
                )
            )

            // Data Kategori Tugas & Jadwal
            _faqItems.add(
                FAQItem(
                    id = 4,
                    question = "Bagaimana cara menambahkan tugas baru?",
                    answer = "Untuk menambahkan tugas baru:\n\n1. Klik tombol '+' pada halaman utama\n2. Isi informasi tugas seperti judul, mata kuliah, tanggal, dll\n3. Tekan 'Simpan' untuk menyimpan tugas baru",
                    category = FAQCategory.TASK
                )
            )

            _faqItems.add(
                FAQItem(
                    id = 5,
                    question = "Bagaimana cara menandai tugas sudah selesai?",
                    answer = "Cukup klik kotak centang di samping tugas untuk menandainya sebagai selesai. Anda juga dapat membatalkan tanda ini dengan mengklik kembali kotak tersebut.",
                    category = FAQCategory.TASK
                )
            )

            _faqItems.add(
                FAQItem(
                    id = 6,
                    question = "Apakah saya bisa menghapus tugas yang sudah dibuat?",
                    answer = "Ya, untuk menghapus tugas, cukup tekan lama pada tugas yang ingin dihapus, kemudian pilih opsi 'Hapus' yang muncul.",
                    category = FAQCategory.TASK
                )
            )

            // Data Kategori Lainnya
            _faqItems.add(
                FAQItem(
                    id = 7,
                    question = "Apakah notifikasi dapat disesuaikan?",
                    answer = "Ya, Anda dapat menyesuaikan notifikasi di halaman 'Keamanan dan Privasi'. Anda dapat mengaktifkan atau menonaktifkan notifikasi dan mengatur jenis notifikasi yang ingin diterima.",
                    category = FAQCategory.OTHER
                )
            )

            _faqItems.add(
                FAQItem(
                    id = 8,
                    question = "Bagaimana cara melaporkan bug atau masalah aplikasi?",
                    answer = "Untuk melaporkan bug atau masalah, silakan kirim email ke support@disiplinpro.id dengan detail masalah yang Anda alami. Tim kami akan merespons secepat mungkin.",
                    category = FAQCategory.OTHER
                )
            )

            _faqItems.add(
                FAQItem(
                    id = 9,
                    question = "Di mana saya bisa mendapatkan bantuan lebih lanjut?",
                    answer = "Anda dapat menghubungi tim dukungan kami melalui email support@disiplinpro.id atau kunjungi situs web kami di www.disiplinpro.id untuk informasi bantuan lebih lanjut.",
                    category = FAQCategory.OTHER
                )
            )

            updateFilteredItems()
            isLoading.value = false
        }
    }

    // Update search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredItems()
    }

    // Update selected category
    fun updateSelectedCategory(category: FAQCategory) {
        _selectedCategory.value = category
        updateFilteredItems()
    }

    // Filter FAQ berdasarkan search query dan kategori
    private fun updateFilteredItems() {
        val query = _searchQuery.value.lowercase()
        val category = _selectedCategory.value

        _filteredFaqItems.value = _faqItems.filter { faqItem ->
            (query.isEmpty() || faqItem.question.lowercase().contains(query) ||
                    faqItem.answer.lowercase().contains(query)) &&
                    (category == FAQCategory.ALL || faqItem.category == category)
        }
    }

    // Fungsi untuk kontak dukungan
    fun contactSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@disiplinpro.id")
            putExtra(Intent.EXTRA_SUBJECT, "Bantuan DisiplinPro")
        }

        val context = getApplication<Application>()
        try {
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Tidak dapat membuka aplikasi email. Silakan kirim email ke support@disiplinpro.id",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

// Data class untuk item FAQ
data class FAQItem(
    val id: Int,
    val question: String,
    val answer: String,
    val category: FAQCategory
)

// Enum untuk kategori FAQ
enum class FAQCategory {
    ALL, ACCOUNT, TASK, OTHER;

    fun toDisplayName(): String {
        return when (this) {
            ALL -> "Semua"
            ACCOUNT -> "Akun"
            TASK -> "Tugas"
            OTHER -> "Lainnya"
        }
    }
}