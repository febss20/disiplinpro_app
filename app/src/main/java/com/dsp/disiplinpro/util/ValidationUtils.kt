package com.dsp.disiplinpro.util

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

/**
 * Utility class untuk validasi input dengan implementasi keamanan yang ketat
 */
object ValidationUtils {

    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    )

    private val USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,20}$")

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Validasi email
     * @return Pair<Boolean, String?> - Pertama: valid atau tidak, Kedua: pesan error (null jika valid)
     */
    fun validateEmail(email: String): Pair<Boolean, String?> {
        if (email.isBlank()) {
            return Pair(false, "Email tidak boleh kosong")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Pair(false, "Format email tidak valid")
        }

        return Pair(true, null)
    }

    /**
     * Validasi password dengan aturan keamanan yang ketat
     */
    fun validatePassword(password: String): Pair<Boolean, String?> {
        if (password.isBlank()) {
            return Pair(false, "Password tidak boleh kosong")
        }

        if (password.length < 8) {
            return Pair(false, "Password minimal 8 karakter")
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return Pair(
                false,
                "Password harus berisi minimal 1 huruf besar, 1 huruf kecil, " +
                        "1 angka, dan 1 karakter khusus (@#$%^&+=!)"
            )
        }

        return Pair(true, null)
    }

    /**
     * Validasi username
     */
    fun validateUsername(username: String): Pair<Boolean, String?> {
        if (username.isBlank()) {
            return Pair(false, "Username tidak boleh kosong")
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return Pair(
                false,
                "Username hanya boleh berisi huruf, angka, dan karakter . _ -" +
                        " dengan panjang 3-20 karakter"
            )
        }

        return Pair(true, null)
    }

    /**
     * Sanitasi input teks untuk menghindari XSS dan injeksi
     */
    fun sanitizeInput(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }

    /**
     * Validasi tanggal format yyyy-MM-dd
     */
    fun validateDate(dateStr: String): Pair<Boolean, String?> {
        if (dateStr.isBlank()) {
            return Pair(false, "Tanggal tidak boleh kosong")
        }

        return try {
            val date = DATE_FORMAT.parse(dateStr)

            val parsedDateStr = DATE_FORMAT.format(date)
            if (parsedDateStr != dateStr) {
                Pair(false, "Format tanggal tidak valid (yyyy-MM-dd)")
            } else {
                Pair(true, null)
            }
        } catch (e: Exception) {
            Pair(false, "Format tanggal tidak valid (yyyy-MM-dd)")
        }
    }

    /**
     * Validasi waktu format HH:mm
     */
    fun validateTime(timeStr: String): Pair<Boolean, String?> {
        if (timeStr.isBlank()) {
            return Pair(false, "Waktu tidak boleh kosong")
        }

        val pattern = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        if (!pattern.matcher(timeStr).matches()) {
            return Pair(false, "Format waktu tidak valid (HH:mm)")
        }

        return Pair(true, null)
    }

    /**
     * Validasi nama mata kuliah
     */
    fun validateCourseName(courseName: String): Pair<Boolean, String?> {
        if (courseName.isBlank()) {
            return Pair(false, "Nama mata kuliah tidak boleh kosong")
        }

        if (courseName.length < 3) {
            return Pair(false, "Nama mata kuliah minimal 3 karakter")
        }

        if (courseName.length > 50) {
            return Pair(false, "Nama mata kuliah maksimal 50 karakter")
        }

        val sanitizedName = sanitizeInput(courseName)
        if (sanitizedName != courseName) {
            return Pair(false, "Nama mata kuliah mengandung karakter yang tidak diperbolehkan")
        }

        return Pair(true, null)
    }

    /**
     * Validasi judul tugas
     */
    fun validateTaskTitle(title: String): Pair<Boolean, String?> {
        if (title.isBlank()) {
            return Pair(false, "Judul tugas tidak boleh kosong")
        }

        if (title.length < 3) {
            return Pair(false, "Judul tugas minimal 3 karakter")
        }

        if (title.length > 100) {
            return Pair(false, "Judul tugas maksimal 100 karakter")
        }

        val sanitizedTitle = sanitizeInput(title)
        if (sanitizedTitle != title) {
            return Pair(false, "Judul tugas mengandung karakter yang tidak diperbolehkan")
        }

        return Pair(true, null)
    }

    /**
     * Validasi deskripsi tugas
     */
    fun validateTaskDescription(description: String): Pair<Boolean, String?> {
        if (description.length > 500) {
            return Pair(false, "Deskripsi tugas maksimal 500 karakter")
        }

        // Sanitasi deskripsi tugas sebagai tambahan keamanan
        val sanitizedDesc = sanitizeInput(description)
        if (sanitizedDesc != description) {
            return Pair(false, "Deskripsi tugas mengandung karakter yang tidak diperbolehkan")
        }

        return Pair(true, null)
    }
}