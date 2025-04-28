package com.example.disiplinpro.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Handler error yang aman, tidak mengekspos informasi sensitif ke pengguna
 */
object SecureErrorHandler {

    private const val TAG = "SecureErrorHandler"

    private const val GENERAL_ERROR = "Terjadi kesalahan. Silakan coba lagi."
    private const val NETWORK_ERROR = "Koneksi internet tidak tersedia. Periksa koneksi Anda."
    private const val TIMEOUT_ERROR = "Waktu permintaan habis. Silakan coba lagi."
    private const val SERVER_ERROR = "Layanan sedang tidak tersedia. Silakan coba beberapa saat lagi."

    private const val AUTH_INVALID_CREDENTIALS = "Email atau password salah."
    private const val AUTH_INVALID_USER = "Akun tidak ditemukan."
    private const val AUTH_USER_COLLISION = "Email sudah digunakan oleh akun lain."
    private const val AUTH_WEAK_PASSWORD = "Password terlalu lemah. Gunakan minimal 8 karakter."

    private const val DATABASE_ERROR = "Gagal mengakses data. Silakan coba lagi."

    /**
     * Menangani error umum
     * @return Pesan error yang aman untuk ditampilkan ke pengguna
     */
    fun handleException(e: Exception, logTag: String? = TAG): String {
        Log.e(logTag ?: TAG, "Error: ${e.message}", e)

        return when (e) {
            is UnknownHostException,
            is FirebaseNetworkException -> NETWORK_ERROR

            is TimeoutException,
            is SocketTimeoutException -> TIMEOUT_ERROR

            is FirebaseAuthInvalidCredentialsException -> AUTH_INVALID_CREDENTIALS
            is FirebaseAuthInvalidUserException -> AUTH_INVALID_USER
            is FirebaseAuthUserCollisionException -> AUTH_USER_COLLISION
            is FirebaseAuthWeakPasswordException -> AUTH_WEAK_PASSWORD

            is FirebaseFirestoreException -> {
                when (e.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.INTERNAL,
                    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> SERVER_ERROR
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Anda tidak memiliki izin untuk operasi ini."
                    else -> DATABASE_ERROR
                }
            }

            is FirebaseException -> {
                if (e.message?.contains("network", ignoreCase = true) == true) {
                    NETWORK_ERROR
                } else {
                    GENERAL_ERROR
                }
            }

            else -> GENERAL_ERROR
        }
    }

    /**
     * Menampilkan error ke pengguna dengan Toast
     */
    fun showError(context: Context, e: Exception, logTag: String? = TAG) {
        val errorMessage = handleException(e, logTag)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    /**
     * Menangani error dari string pesan
     */
    fun handleErrorMessage(errorMessage: String?, logTag: String? = TAG): String {
        Log.e(logTag ?: TAG, "Error message: $errorMessage")

        return when {
            errorMessage?.contains("network", ignoreCase = true) == true -> NETWORK_ERROR
            errorMessage?.contains("timeout", ignoreCase = true) == true -> TIMEOUT_ERROR
            errorMessage?.contains("permission", ignoreCase = true) == true -> "Anda tidak memiliki izin untuk operasi ini."
            errorMessage?.contains("invalid credential", ignoreCase = true) == true -> AUTH_INVALID_CREDENTIALS
            errorMessage?.contains("no user record", ignoreCase = true) == true -> AUTH_INVALID_USER
            errorMessage?.contains("email already in use", ignoreCase = true) == true -> AUTH_USER_COLLISION
            errorMessage?.contains("weak password", ignoreCase = true) == true -> AUTH_WEAK_PASSWORD
            errorMessage?.contains("server", ignoreCase = true) == true -> SERVER_ERROR
            else -> GENERAL_ERROR
        }
    }

    /**
     * Mencatat error untuk analisis tanpa mengekspos informasi sensitif
     */
    fun logError(e: Exception, logTag: String? = TAG, userId: String? = null) {
        val tag = logTag ?: TAG

        val sanitizedMessage = e.message?.replace(Regex("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+"), "[EMAIL]") ?: "No message"

        Log.e(tag, "Error: $sanitizedMessage, User: ${userId ?: "Unknown"}", e)
    }

    /**
     * Menangani error pada operasi database dengan pesan yang aman
     */
    fun handleDatabaseError(e: Exception, operation: String): String {
        Log.e(TAG, "Database error during $operation: ${e.message}", e)

        return when (operation) {
            "read" -> "Gagal memuat data. Silakan coba lagi."
            "create" -> "Gagal membuat data baru. Silakan coba lagi."
            "update" -> "Gagal memperbarui data. Silakan coba lagi."
            "delete" -> "Gagal menghapus data. Silakan coba lagi."
            else -> DATABASE_ERROR
        }
    }
}